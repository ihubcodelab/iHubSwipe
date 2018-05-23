"""
jsonUpdaterTool v1.0
@author - Sam Baskin
Tool for updating old JSON directory files to the new format
Make a Directory named 'output' in the folder with the json files
Then move this script to that directory as well, and run it
"""

import os
import json

def loopThruJSON():
    directory = os.getcwd()
    #loop through every .json file in the directory
    for filename in os.listdir(directory):
        if filename.endswith(".json"):
            
            with open(filename) as json_data:
                inputFile = json.load(json_data)
                json_data.close()
                newFile = {}
                #some portions might be null on the input file
                if inputFile['id'].isdigit() and len(inputFile['id'])==9:
                    newFile['id'] = inputFile['id']
                elif inputFile['cardNumber'][0]==';':
                    newFile['id']= inputFile['cardNumber'][6:15]
                    print(newFile['id'])
                else:
                    newFile['id'] = inputFile['cardNumber']
                newFile['name'] = inputFile['name']
                newFile['email'] = inputFile['email']
                #certs
                #will hold certs to add
                newCerts = []
                if inputFile['certifications'] != '':
                    certs = inputFile['certifications'].split(', ')
                    for cert in certs:
                        if 'laser' in cert.lower():
                            newCerts.append(newCert("Laser Cutter","09-17-2018", False))
                        if '3d' in cert.lower():
                            newCerts.append(newCert("3D Printer","09-17-2018", False))
                        if 'red' in cert.lower():
                            newCerts.append(newCert("Red","09-17-2018", True))
                        if 'yellow' in cert.lower():
                            newCerts.append(newCert("Yellow","09-17-2018", True))
                        if 'green' in cert.lower():
                            newCerts.append(newCert("Green","09-17-2018", True))
                if 'shopCertification' in inputFile: #not all records have this value
                    shop = inputFile['shopCertification']
                    if 'red' in shop.lower():
                        newCerts.append(newCert("Red","09-17-2018", True))
                    if 'yellow' in shop.lower():
                        newCerts.append(newCert("Yellow","09-17-2018", True))
                    if 'green' in shop.lower():
                        newCerts.append(newCert("Green","09-17-2018", True))
                
                newFile['certifications'] = newCerts
                
                newFile['notes'] = inputFile['notes']
                if 'signedWaiver' not in inputFile:
                    newFile['signedWaiver'] = "No"
                else:
                    newFile['signedWaiver'] = inputFile['signedWaiver']
                newFile['timesVisited'] = inputFile['timesVisited']
                newFile['strikes'] = inputFile['strikes']
                if 'timeStampHistory' in inputFile:
                    newFile['timeStampHistory'] = inputFile['timeStampHistory']

                output = os.path.join(directory, 'output', filename)
                with open(output, 'w') as outfile:
                    json.dump(newFile, outfile, indent=4, sort_keys=True)
                    outfile.close()

                print(inputFile['name'])    
                continue
                
        else:
            #print(os.path.join(directory, filename))
            continue

def newCert(name, exp, isShopCert):
    cert = {}
    cert['certName'] = name
    cert['expiration'] = exp
    cert['isShopCert'] = isShopCert
    return cert

if __name__ == '__main__':
    print("Starting tool...")
    loopThruJSON()
