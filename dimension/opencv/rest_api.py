from flask import Flask,jsonify, request
from detected_object import DetectedObject
import flasktest

app = Flask(__name__)

#car = DetectedObject('Car', 170, 205, 2305)
all_objects = []



@app.route('/')
def dimension():
	return "Dimension - handheld measuring made easy"
	



@app.route('/objects')
def get_object():
	return jsonify(all_objects)



@app.route('/objects', methods=['POST'])
def post_object():
	#print('Nu är vi här istället')
	#print(request.get_json())
	all_objects.append(request.get_json())
	return '', 204



#if __name__ == '__main__':
app.run(host='0.0.0.0')





#request.post('http://127.0.0.1:5000/objects', json={'hej': 1})


