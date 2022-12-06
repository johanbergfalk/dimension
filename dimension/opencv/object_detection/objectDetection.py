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

import os
import cv2
import numpy as np
import RPi.GPIO as GPIO
from tflite_runtime.interpreter import Interpreter
from VideoStream import VideoStream
import math
import calculatemetrics
from datetime import datetime
from detected_object import DetectedObject
from PIL import Image as PIL_IMG
from io import BytesIO

import base64
import time
import board
import busio
import adafruit_lidarlite

# Create library object using our Bus I2C port
i2c = busio.I2C(board.SCL, board.SDA)

# Default configuration, with only i2c wires
sensor = adafruit_lidarlite.LIDARLite(i2c)

#Clear the initial inaccurate measurements from the sensor
try:
    for i in range(10):
        sensor.distance
except:
    pass


#Tell RPi to use pin numbers and not BCM when setting up gpio
#GPIO.setmode(GPIO.BOARD)
      
# Create and initialize state-variables used to handle activation by single button press
searchActivated = False
# NOT USED # timeoutDelay = 5 # In seconds. After button is pressed, the searchActivated will be set to false after timeoutDelay
# NOT USED # ledRED = 40
# NOT USED # ledYELLOW = 38
# NOT USED # ledGREEN = 36
buttonPin = 26

# Initialize pins
# NOT USED # GPIO.setup(ledRED, GPIO.OUT)
# NOT USED # GPIO.setup(ledYELLOW, GPIO.OUT)
# NOT USED # GPIO.setup(ledGREEN, GPIO.OUT)
GPIO.setup(buttonPin, GPIO.IN, pull_up_down = GPIO.PUD_DOWN)

# NOT USED # GPIO.output(ledYELLOW, GPIO.HIGH)

MODEL_NAME = 'coco_ssd_mobilenet_v1/'
GRAPH_NAME = 'detect.tflite'
LABELMAP_NAME = 'labelmap.txt'
MIN_CONF_THRESHOLD = 0.5
imW = 1280
imH = 720
centerX = int(imW/2)
centerY = int(imH/2)

CWD = os.getcwd()
PATH_TFMODEL = os.path.join(CWD, MODEL_NAME, GRAPH_NAME)
PATH_LABELS = os.path.join(CWD, MODEL_NAME, LABELMAP_NAME)

with open(PATH_LABELS, 'r') as file:
    labels = [line.strip() for line in file.readlines()]

if labels[0] == '???':
    del(labels[0])


interpreter = Interpreter(model_path=PATH_TFMODEL)
interpreter.allocate_tensors()

# Save the object of interest to draw box around
bestObjectPosition = [0, 0, 0, 0] #(xmin, ymin, xmax, ymax)
bestObjectImg = None
bestObjectDistance = None
bestObjectClass = None
bestObjectLidar = None
frameCount = 0



#Is the detected object the one pointed at by the LiDAR?
# min = top-left corner, max = bottom-right corner
def objectIsCenter(xmin, ymin, xmax, ymax):
    if xmin <= centerX and centerX <= xmax and ymin <= centerY and centerY <= ymax:
        return True
    else:
        return False

def buttonPressed(pin):
    global searchActivated, sensor
    global frameCount, bestObjectClass, bestObjectImg, bestObjectDistance, bestObjectPosition, bestObjectLidar
    print("Button pressed")
    if searchActivated:
        return
    else:
        
        print(sensor.distance)
        searchActivated = True
        bestObjectPosition = None
        bestObjectDistance = None
        bestObjectLidar = None
        bestObjectImg = None
        bestObjectClass = None
        frameCount = 3 #How many frames to sample
        

def distanceFromCenter(xmin, ymin, xmax, ymax):
    xObjCenter = xmin + (int(round((xmax - xmin) / 2)))
    yObjCenter = ymin + (int(round((ymax - ymin) / 2)))
    dist = math.sqrt(((xObjCenter - centerX) ** 2) + ((yObjCenter - centerY) ** 2))
    return int(dist)

GPIO.add_event_detect(buttonPin, GPIO.RISING, callback = buttonPressed, bouncetime=500)







# Get model details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
model_height = input_details[0]['shape'][1]
model_width = input_details[0]['shape'][2]


videostream = VideoStream(resolution=(imW, imH), framerate=30).start()

cv2.namedWindow('Object detector', cv2.WINDOW_NORMAL)

while True:

   

    frame = videostream.read()
    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    frame_rgb = cv2.resize(frame_rgb, (model_width, model_height))
    input_data = np.expand_dims(frame_rgb, axis=0)

    if searchActivated and frameCount > 0:
        frameCount = frameCount - 1
        
        # Perform the actual detection by running the model with the image as input
        interpreter.set_tensor(input_details[0]['index'],input_data)
        interpreter.invoke()

        # Retrieve detection results
        boxes = interpreter.get_tensor(output_details[0]['index'])[0] # Bounding box coordinates of detected objects
        classes = interpreter.get_tensor(output_details[1]['index'])[0] # Class index of detected objects
        scores = interpreter.get_tensor(output_details[2]['index'])[0] # Confidence of detected objects
        
            
        # Loop over all detections and draw detection box if confidence is above minimum threshold

        for i in range(len(scores)):
            if ((scores[i] > MIN_CONF_THRESHOLD) and (scores[i] <= 1.0)):
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
                        successfulMeasures = 10
                        avgMeasure = 0
                        for i in range(10):
                            try:
                                avgMeasure += sensor.distance
                            except:
                                successfulMeasures -= 1
                        bestObjectLidar = (avgMeasure / successfulMeasures) - 12
                        
                    if distanceFromCenter(xmin, ymin, xmax, ymax) < bestObjectDistance:
                        bestObjectDistance = distanceFromCenter(xmin, ymin, xmax, ymax)
                        bestObjectPosition = [xmin, ymin, xmax, ymax]
                        bestObjectImg = frame.copy()
                        bestObjectClass = labels[int(classes[i])]
                        successfulMeasures = 10
                        avgMeasure = 0
                        for i in range(10):
                            try:
                                avgMeasure += sensor.distance
                            except:
                                successfulMeasures -= 1
                        bestObjectLidar = (avgMeasure / successfulMeasures) - 12
                 
        if frameCount == 0:
            searchActivated = False
            if bestObjectDistance != None:
                detectedImage = bestObjectImg[bestObjectPosition[1]:bestObjectPosition[3], bestObjectPosition[0]:bestObjectPosition[2]]
                
                
                
                matchwidth = calculatemetrics.px_to_metric(1280, 160, bestObjectPosition[2] - bestObjectPosition[0], bestObjectLidar)
                matchheight = calculatemetrics.px_to_metric(720, 90, bestObjectPosition[3] - bestObjectPosition[1], bestObjectLidar)
                
                detectedImage = cv2.cvtColor(detectedImage, cv2.COLOR_BGR2RGB)
                buff = BytesIO()
                data = PIL_IMG.fromarray(detectedImage)
                data.save(buff, format="JPEG")
                img64 = base64.b64encode(buff.getvalue()).decode("utf-8")
                
                
                
                
                match = DetectedObject(bestObjectClass, int(matchheight), int(matchwidth), bestObjectLidar, img64)
                match.object_list()
            else:
                print("No match found")
                
                
    
    # All the results have been drawn on the frame, so it's time to display it.
    cv2.imshow('Object detector', frame)

    

   
    
    # Press 'q' to quit
    if cv2.waitKey(1) == ord('q'):
        break

# Clean up
cv2.destroyAllWindows()
videostream.stop()
GPIO.cleanup()

