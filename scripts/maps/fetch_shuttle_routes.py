#!/usr/bin/env python3
import base64
import datetime
from datetime import timedelta
from itertools import chain
from os import makedirs
import re

import requests

def dump_stops(route_id, stops):
    filename = f'stops/{route_id}_stops.txt'
    with open(filename, 'w') as outfile:
        for stop in stops:
            outfile.write('{},{}\t{}\t{}\n'.format(
                stop['latLng'][0], stop['latLng'][1], stop['name'], stop['id']))

def dump_route_trace(route_id, trace):
    filename = f'traces/{route_id}.txt'
    with open(filename, 'w') as outfile:
        for latlng in trace:
            outfile.write(f'{latlng[0]},{latlng[1]};')

def main():
    CAP_METRO_URL = 'https://capmetro.org/'
    CAP_METRO_META_URL = CAP_METRO_URL + 'schedmap/config.jsn'
    CAP_METRO_ROUTE_URL = CAP_METRO_URL + 'planner/s_routetrace.php'
    TODAY = datetime.date.today()
    WEEKDAY = TODAY.weekday()
    NEXTSUNDAY = (TODAY + timedelta(days=6 - WEEKDAY)).strftime('%m/%d/%Y')
    NEXTMONDAY = (TODAY + timedelta(days=7 - WEEKDAY)).strftime('%m/%d/%Y')
    print(f'Using {NEXTMONDAY} as benchmark for weekday routes')
    print(f'Using {NEXTSUNDAY} as benchmark for weekend routes')

    makedirs('stops', exist_ok=True)
    makedirs('traces', exist_ok=True)

    cfg = requests.get(CAP_METRO_META_URL).json()

    params = {'opts': 123}
    routes = [r['route'] for r in cfg['tabConfig']['routes'] if re.match('6..', r['route'])]
    for route in routes:
        print('Fetching route:', route)
        params['route'] = route
        if route in ['681', '682']:
            # Only run on Sundays
            params['date'] = NEXTSUNDAY
        else:
            params['date'] = NEXTMONDAY
        result = requests.get(CAP_METRO_ROUTE_URL, params=params).json()
        if result['status'] != 'OK':
            print(result['status'])
            continue
        dump_stops(result['route'],
                chain.from_iterable([x['stops'] for x in result['dirs']]))
        dump_route_trace(result['route'],
                chain.from_iterable([x['trace'] for x in result['dirs']]))

if __name__ == '__main__':
    main()
