class DetectedObject:

    def __init_(self, object_type, height, width, distance):
        self.object_type = object_type
        self.height = height
        self.width = width
        self.distance = distance

    def object_list(self):
        json_object = {
            'objectType': self.object_type,
            'height': self.height,
            'widht': self.width,
            'distance': self.distance}

        return json_object
