from flask import Flask, jsonify
from model.detected_object import DetectedObject

app = Flask(__name__)


@app.route('/')
def dimension():
    return "Dimension - handheld measuring made easy!"


@app.route('/objects')
def get_object():
    return jsonify(all_objects)


# Tests
car = DetectedObject('Car', 170, 205, 2305)
person = DetectedObject('Person', 184, 43, 3250)

all_objects = [car.object_list(), person.object_list()]
