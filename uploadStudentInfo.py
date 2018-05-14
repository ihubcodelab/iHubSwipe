"""
Tool to upload student info csv to AWS
Get the CSV file containing info about students through Tom Wavering
May want to change table or what gets uploaded
Upload will take a while
"""

import boto3
import csv
import decimal

dynamodb = boto3.resource('dynamodb', region_name='us-east-2')

table = dynamodb.Table('studentInfo')

with open("STUDENT INFO.csv", newline='') as csvfile:
    reader = csv.reader(csvfile, delimiter=',', quotechar='|')
    count = 0
    for row in reader:
        count+=1
        studentId = row[0]
        name = row[2][:-1] + " " + row[1][1:]
        email = row[3]
        phone = row[4]
        college = row[5]
        year = row[7]
        major = row[6]

        if (phone == ""):
            table.put_item(
                Item={
                    'id':studentId,
                    'name' : name,
                    'email' : email,
                    'college' : college,
                    'year' : year,
                    'major' : major
                }
            )
        else:
            table.put_item(
                Item={
                    'id':studentId,
                    'name' : name,
                    'email' : email,
                    'phone' : phone,
                    'college' : college,
                    'year' : year,
                    'major' : major
                }
            )
        if (count%100==0):
            print(str(count) + " records uploaded")


    