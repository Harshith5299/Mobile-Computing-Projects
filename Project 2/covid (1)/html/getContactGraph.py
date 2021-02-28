import sqlite3
from sqlite3 import Error
import sys
from math import cos, asin, sqrt, pi
import math


def createConnection(db_file):
    conn = None
    try:
        conn = sqlite3.connect(db_file)
    except Error as e:
        print(e)

    return conn

def selectAllLocations(conn, date):
    cur = conn.cursor()
    cur.execute("SELECT _latitude, _longitude, _time_location FROM locationTable")

    rows = cur.fetchall()
    
    subjectLoc = {}
    
    for row in rows:
        lat = row[0]
        lon = row[1]
        curDate = str(row[2])

        if lat!=0 and lon!=0 and curDate!='':
            curDate = int(curDate[:8])
            lat = float(lat) / 1000000
            lon = float(lon) / 1000000
            
            if curDate > date-7 and curDate <= date:
                if curDate not in subjectLoc:
                    subjectLoc[curDate] = []
                subjectLoc[curDate].append([lat, lon])
    
    cur.close()
    
    return subjectLoc

def getContactMatrix(allSubjectsLocations, subjectID):
    contactMatrix = {}
    
    subjectLoc = allSubjectsLocations[subjectID - 1]
    for key in subjectLoc:
        contactMatrix[key] = []
        for i in range(0, 12):
            if i != subjectID - 1:
                curSubject = allSubjectsLocations[i]
                contact = 0
                if key in curSubject:
                    for lat1, lon1 in subjectLoc[key]:
                        for lat2, lon2 in curSubject[key]:
                            if distance(lat1,lon1,lat2,lon2) <= 5.001:
                                contact = 1
                                break
                        if contact == 1:
                            break
                contactMatrix[key].append(contact)
            else:
                contactMatrix[key].append(1)
    
    return contactMatrix

def getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2):
    R = 6371 # Radius of the earth in km
    dLat = deg2rad(lat2-lat1) #deg2rad below
    dLon = deg2rad(lon2-lon1)
    a = math.sin(dLat/2) * math.sin(dLat/2) + math.cos(deg2rad(lat1)) * math.cos(deg2rad(lat2)) * math.sin(dLon/2) * math.sin(dLon/2)
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a));
    d = R * c # Distance in km
    return d

def deg2rad(deg):
    return deg * (math.pi/180)
    
def distance(lat1, lon1, lat2, lon2):
    p = pi/180
    a = 0.5 - cos((lat2-lat1)*p)/2 + cos(lat1*p) * cos(lat2*p) * (1-cos((lon2-lon1)*p))/2
    return 12742 * asin(sqrt(a)) #2*R*asin...

def beautifyDate(date):
    readableDate = str(date)
    readableDate = readableDate[0:4] + "/" + readableDate[4:6] + "/" + readableDate[6:8]
    return readableDate
    

def main():
    databasePrefix = r"C:/nginx/html/dbs/LifeMap_GS"
    f = open(sys.argv[3], "w")
    
    subjectID = int(sys.argv[1])
    date = sys.argv[2]
    
    date = date.replace('/', '')
    date = date.replace('-', '')
    date = int(date)
    
    # create a database connection
    conns = []
    for i in range(1, 13):
        conns.append(createConnection(databasePrefix + str(i) + '.db'))
    
    subjectLoc = selectAllLocations(conns[subjectID - 1], date)
    allSubjectsLocations = []
    
    if len(subjectLoc) > 0:
        for i in range(0,12):
            if i != subjectID - 1:
                allSubjectsLocations.append(selectAllLocations(conns[i], date))
            else:
                allSubjectsLocations.append(subjectLoc)
        
        contactMatrix = getContactMatrix(allSubjectsLocations, subjectID)
        
        f.write("Contact Matrix for Subject: " + str(subjectID) + "\n\n")            
        
        for key in range(date-7+1, date+1):
            if key in contactMatrix:
                f.write("Date: " + beautifyDate(key) +  " - Contact List: " +str(contactMatrix[key]) + "\n")
            else:
                noContactList = [0,0,0,0,0,0,0,0,0,0,0,0]
                noContactList[subjectID-1] = 1
                f.write("Date: " + beautifyDate(key) +  " - Contact List: " +str(noContactList) + "\n")
        
        f.write("\n\nNote: Subject contact to itself is always considered 1.\n\n")
    else:
        f.write("No locations found for subject " + str(subjectID) + " in the past 7 days of given date " + beautifyDate(date) + ".\n")
    f.close()
    
    for conn in conns:
        conn.close()

if __name__ == '__main__':
    main()

