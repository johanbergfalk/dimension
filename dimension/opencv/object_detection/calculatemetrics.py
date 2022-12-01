import math

def px_to_metric(res_wh, fov_wh, k, d):
	px_to_metric_result = d * math.tan(0.5 * k * (fov_wh * (3.14159 / 180) / res_wh))
	return px_to_metric_result


