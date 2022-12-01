from flask import Flask,jsonify, request
import requests

class DetectedObject:
	
	def __init__(self, object_type, height, width, distance):
		self.object_type = object_type
		self.height = height
		self.width = width
		self.distance = distance
	
	
	def object_list(self):
		json_object = {'objectType': self.object_type, 'height': self.height, 'width': self.width, 'distance': self.distance}
		
		requests.post('http://127.0.0.1:5000/objects', json=json_object)
		
		return json_object
