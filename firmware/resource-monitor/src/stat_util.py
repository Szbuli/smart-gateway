
def smooth(current, prev, smoothingFactor):
    return current * smoothingFactor + (1 - smoothingFactor) * prev
