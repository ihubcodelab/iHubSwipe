"""
iHub Analytics
@author - Sam Baskin

"""

import boto3
import csv
import decimal

parser = argparse.ArgumentParser(description='Tool to retrieve GroupMe messages and output them to a CSV file.')
parser.add_argument('-d', '--daily', help='Sort by day', action="store_true")
parser.add_argument('-h', '--hourly', help='Sort by hour', action="store_true")
parser.add_argument('-b', '--both', help='Both Hourly and daily', action="store_true")



# Start Point
if __name__ == '__main__':
    print("starting analytics")
