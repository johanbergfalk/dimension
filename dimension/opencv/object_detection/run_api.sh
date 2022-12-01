#!/bin/sh

export FLASK_APP=./rest_api.py

tflite-env run flask --debug run -h 0.0.0.0
