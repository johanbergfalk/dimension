######## Webcam Object Detection Using Tensorflow-trained Classifier #########
#
# Author: Evan Juras
# Date: 10/27/19
# Description: 
# This program uses a TensorFlow Lite model to perform object detection on a live webcam
# feed. It draws boxes and scores around the objects of interest in each frame from the
# webcam. To improve FPS, the webcam object runs in a separate thread from the main program.
# This script will work with either a Picamera or regular USB webcam.
#
# This code is based off the TensorFlow Lite image classification example at:
# https://github.com/tensorflow/tensorflow/blob/master/tensorflow/lite/examples/python/label_image.py
#
# I added my own method of drawing boxes and labels using OpenCV.
# 
# Modified by: Shawn Hymel
# Date: 09/22/20
# Description:
# Added ability to resize cv2 window and added center dot coordinates of each detected object.
# Objects and center coordinates are printed to console.
#
################################################################
#
# Modified by: Team Dimension - Chalmers
# Date: 11/24/22
# Description:
# Renamed class to 'main.py'.
# Optimized code and added features for Raspberry Pi 4 8GB to be
# used with our application to measure objects automatically.
# Added LiDAR support to measure distance to object. Only registers objects
# within the mark of the LiDAR. Added calculations to retrieve width and
# height of an object with the help of LiDAR and camera FOV. Pin signaling
# status aswell as integrated our own REST API to send results to Android app.
#
################################################################

# Import packages
import os
import argparse
import cv2
import numpy as np
import sys
import time
import RPi.GPIO as GPIO
from threading import Thread, Timer
import math
import calculatemetrics
from datetime import datetime
import importlib.util
from detected_object import DetectedObject


# Use pin numbering and not BCM
GPIO.setmode(GPIO.BOARD)

# Create and initialize state-variables used to handle activation by single button press
searchActivated = False
timeoutDelay = 5 # In seconds. After button is pressed, the searchActivated will be set to false after timeoutDelay
ledRED = 40
ledYELLOW = 38
ledGREEN = 36
buttonPin = 37

# Initialize pins
GPIO.setup(ledRED, GPIO.OUT)
GPIO.setup(ledYELLOW, GPIO.OUT)
GPIO.setup(ledGREEN, GPIO.OUT)
GPIO.setup(buttonPin, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)

GPIO.output(ledYELLOW, GPIO.HIGH)



# Define VideoStream class to handle streaming of video from webcam in separate processing thread
# Source - Adrian Rosebrock, PyImageSearch: https://www.pyimagesearch.com/2015/12/28/increasing-raspberry-pi-fps-with-python-and-opencv/
class VideoStream:
    """Camera object that controls video streaming from the Picamera"""
    def __init__(self,resolution=(1280,720),framerate=30):
        # Initialize the PiCamera and the camera image stream
        self.stream = cv2.VideoCapture(0)
        ret = self.stream.set(cv2.CAP_PROP_FOURCC, cv2.VideoWriter_fourcc(*'MJPG'))
        ret = self.stream.set(3,resolution[0])
        ret = self.stream.set(4,resolution[1])
            
        # Read first frame from the stream
        (self.grabbed, self.frame) = self.stream.read()

	# Variable to control when the camera is stopped
        self.stopped = False

    def start(self):
	# Start the thread that reads frames from the video stream
        Thread(target=self.update,args=()).start()
        return self

    def update(self):
        # Keep looping indefinitely until the thread is stopped
        while True:
            # If the camera is stopped, stop the thread
            if self.stopped:
                # Close camera resources
                self.stream.release()
                return

            # Otherwise, grab the next frame from the stream
            (self.grabbed, self.frame) = self.stream.read()

    def read(self):
	# Return the most recent frame
        return self.frame

    def stop(self):
	# Indicate that the camera and thread should be stopped
        self.stopped = True

# Define and parse input arguments
parser = argparse.ArgumentParser()
parser.add_argument('--modeldir', help='Folder the .tflite file is located in',
                    required=True)
parser.add_argument('--graph', help='Name of the .tflite file, if different than detect.tflite',
                    default='detect.tflite')
parser.add_argument('--labels', help='Name of the labelmap file, if different than labelmap.txt',
                    default='labelmap.txt')
parser.add_argument('--threshold', help='Minimum confidence threshold for displaying detected objects',
                    default=0.5)
parser.add_argument('--resolution', help='Desired webcam resolution in WxH. If the webcam does not support the resolution entered, errors may occur.',
                    default='1280x720')
parser.add_argument('--edgetpu', help='Use Coral Edge TPU Accelerator to speed up detection',
                    action='store_true')

args = parser.parse_args()

MODEL_NAME = args.modeldir
GRAPH_NAME = args.graph
LABELMAP_NAME = args.labels
min_conf_threshold = float(args.threshold)
resW, resH = args.resolution.split('x')


imW, imH = int(resW), int(resH)
centerX = int(imW/2)
centerY = int(imH/2)
use_TPU = args.edgetpu

# Import TensorFlow libraries
# If tflite_runtime is installed, import interpreter from tflite_runtime, else import from regular tensorflow
# If using Coral Edge TPU, import the load_delegate library
pkg = importlib.util.find_spec('tflite_runtime')
if pkg:
    from tflite_runtime.interpreter import Interpreter
    if use_TPU:
        from tflite_runtime.interpreter import load_delegate
else:
    from tensorflow.lite.python.interpreter import Interpreter
    if use_TPU:
        from tensorflow.lite.python.interpreter import load_delegate

# If using Edge TPU, assign filename for Edge TPU model
if use_TPU:
    # If user has specified the name of the .tflite file, use that name, otherwise use default 'edgetpu.tflite'
    if (GRAPH_NAME == 'detect.tflite'):
        GRAPH_NAME = 'edgetpu.tflite'       

# Get path to current working directory
CWD_PATH = os.getcwd()


# Path to .tflite file, which contains the model that is used for object detection
PATH_TO_CKPT = os.path.join(CWD_PATH,MODEL_NAME,GRAPH_NAME)

# Path to label map file
PATH_TO_LABELS = os.path.join(CWD_PATH,MODEL_NAME,LABELMAP_NAME)

# Load the label map
with open(PATH_TO_LABELS, 'r') as f:
    labels = [line.strip() for line in f.readlines()]

# Have to do a weird fix for label map if using the COCO "starter model" from
# https://www.tensorflow.org/lite/models/object_detection/overview
# First label is '???', which has to be removed.
if labels[0] == '???':
    del(labels[0])

# Load the Tensorflow Lite model.
# If using Edge TPU, use special load_delegate argument
if use_TPU:
    interpreter = Interpreter(model_path=PATH_TO_CKPT,
                              experimental_delegates=[load_delegate('libedgetpu.so.1.0')])
    print(PATH_TO_CKPT)
else:
    interpreter = Interpreter(model_path=PATH_TO_CKPT)

interpreter.allocate_tensors()





# Save the object of interest to draw box around
bestObjectPosition = [0, 0, 0, 0] #(xmin, ymin, xmax, ymax)
bestObjectImg = None
bestObjectDistance = None
bestObjectClass = None
frameCount = 0

#Is the detected object the one pointed at by the LiDAR?
# min = top-left corner, max = bottom-right corner
def objectIsCenter(xmin, ymin, xmax, ymax):
    if xmin <= centerX and centerX <= xmax and ymin <= centerY and centerY <= ymax:
        return True
    else:
        return False

#TODO: Comment
def buttonPressed(pin):
    global searchActivated
    global frameCount
    print("Button pressed")
    if searchActivated:
        return
    else:
        searchActivated = True
        bestObjectPosition = None
        bestObjectDistance = None
        frameCount = 3 #How many frames to sample
        print("Button pin reading: " + str(GPIO.input(buttonPin)))
        


def distanceFromCenter(xmin, ymin, xmax, ymax):
    xObjCenter = xmin + (int(round((xmax - xmin) / 2)))
    yObjCenter = ymin + (int(round((ymax - ymin) / 2)))
    xCenter, yCenter = int(imW/2),int(imH/2)
    dist = math.sqrt(((xObjCenter - xCenter) ** 2) + ((yObjCenter - yCenter) ** 2))
    return int(dist)
    

GPIO.add_event_detect(buttonPin, GPIO.RISING, callback = buttonPressed, bouncetime=500)



# Get model details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
height = input_details[0]['shape'][1]
width = input_details[0]['shape'][2]

floating_model = (input_details[0]['dtype'] == np.float32)

input_mean = 127.5
input_std = 127.5

# Initialize frame rate calculation
frame_rate_calc = 1
freq = cv2.getTickFrequency()


#sender = DetectedObject('Car', 180, 160, 5000)
#sender.object_list()





# Initialize video stream
videostream = VideoStream(resolution=(imW,imH),framerate=30).start()
time.sleep(1)

# Create window
cv2.namedWindow('Object detector', cv2.WINDOW_NORMAL)


#for frame1 in camera.capture_continuous(rawCapture, format="bgr",use_video_port=True):
while True:
    
    # Start timer (for calculating frame rate)
    t1 = cv2.getTickCount()
    
    # Grab frame from video stream
    frame1 = videostream.read()

    # Acquire frame and resize to expected shape [1xHxWx3]
    frame = frame1.copy()
    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    frame_resized = cv2.resize(frame_rgb, (width, height))
    input_data = np.expand_dims(frame_resized, axis=0)

    # Normalize pixel values if using a floating model (i.e. if model is non-quantized)
    if floating_model:
        input_data = (np.float32(input_data) - input_mean) / input_std

    if searchActivated and frameCount > 0:
        frameCount = frameCount - 1
        print("Frame count: " + str(frameCount))
        # Perform the actual detection by running the model with the image as input
        interpreter.set_tensor(input_details[0]['index'],input_data)
        interpreter.invoke()

        # Retrieve detection results
        boxes = interpreter.get_tensor(output_details[0]['index'])[0] # Bounding box coordinates of detected objects
        classes = interpreter.get_tensor(output_details[1]['index'])[0] # Class index of detected objects
        scores = interpreter.get_tensor(output_details[2]['index'])[0] # Confidence of detected objects
        num = interpreter.get_tensor(output_details[3]['index'])[0]  # Total number of detected objects 
        
            
        # Loop over all detections and draw detection box if confidence is above minimum threshold

        for i in range(len(scores)):
            if ((scores[i] > min_conf_threshold) and (scores[i] <= 1.0)):
                print("Objects detected")
                # Get bounding box coordinates and draw box
                # Interpreter can return coordinates that are outside of image dimensions, need to force them to be within image using max() and min()
                ymin = int(max(1,(boxes[i][0] * imH)))
                xmin = int(max(1,(boxes[i][1] * imW)))
                ymax = int(min(imH,(boxes[i][2] * imH)))
                xmax = int(min(imW,(boxes[i][3] * imW)))
                
                # check if the detected object is within center
                if objectIsCenter(xmin, ymin, xmax, ymax):
                    if bestObjectDistance == None:
                        bestObjectDistance = distanceFromCenter(xmin, ymin, xmax, ymax)
                        bestObjectPosition = [xmin, ymin, xmax, ymax]
                        bestObjectImg = frame.copy()
                        bestObjectClass = labels[int(classes[i])]
                        
                    if distanceFromCenter(xmin, ymin, xmax, ymax) < bestObjectDistance:
                        bestObjectDistance = distanceFromCenter(xmin, ymin, xmax, ymax)
                        bestObjectPosition = [xmin, ymin, xmax, ymax]
                        bestObjectImg = frame.copy()
                        bestObjectClass = labels[int(classes[i])]
                        
            
                
        if frameCount == 0:
            searchActivated = False
            if bestObjectDistance != None:
                #bestObjectImg = bestObjectImg[bestObjectPosition[1]:bestObjectPosition[3], bestObjectPosition[0]:bestObjectPosition[2]]
                print("Best match: " + bestObjectClass)
                cv2.putText(bestObjectImg, bestObjectClass, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2) # Draw label text
                filenameSave = bestObjectClass + "_"
                filenameSave = filenameSave + str(datetime.now())
                filenameSave = filenameSave + '.jpg'
                matchwidth = calculatemetrics.px_to_metric(1280, 160, bestObjectPosition[2] - bestObjectPosition[0], 100)
                matchheight = calculatemetrics.px_to_metric(720, 90, bestObjectPosition[3] - bestObjectPosition[1], 100)
                #cv2.imwrite(filenameSave, bestObjectImg)
                match = DetectedObject(bestObjectClass, int(matchheight), int(matchwidth), 100)
                match.object_list()
                #print("Output saved as: " + filenameSave)
                
      

    

            
    # Draw framerate in corner of frame
    cv2.putText(frame,'FPS: {0:.2f}'.format(frame_rate_calc),(30,50),cv2.FONT_HERSHEY_SIMPLEX,1,(255,255,0),2,cv2.LINE_AA)
    
    # Draw circle in center of screen
    
    cv2.circle(frame, ((int(imW/2),int(imH/2))), 10, (0,0,255), thickness=2)

    # All the results have been drawn on the frame, so it's time to display it.
    cv2.imshow('Object detector', frame)

    # Calculate framerate
    t2 = cv2.getTickCount()
    time1 = (t2-t1)/freq
    frame_rate_calc= 1/time1

    
    
    # Press 'q' to quit
    if cv2.waitKey(1) == ord('q'):
        break

# Clean up
cv2.destroyAllWindows()
videostream.stop()
GPIO.cleanup()
