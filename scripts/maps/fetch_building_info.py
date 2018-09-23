#!/usr/bin/env python3
import re
import sqlite3

import requests
import pandas as pd

BUILDINGS_INFO_URL = 'https://maps.utexas.edu/buildings'
BUILDING_INFO_URL = f'{BUILDINGS_INFO_URL}/utm/{{0}}'

def fetch_nice_building_names():
    return (pd.concat(pd.read_html(BUILDINGS_INFO_URL), ignore_index=True)
              .rename(columns={'Abbr.': 'abbr', 'Name': 'pretty_name'})
              .drop('Occupied', axis='columns')
              .set_index('abbr'))


def fetch_building_locations():
    BUILDING_GEOJSON_URL = 'https://maps.utexas.edu/data/utm.json'
    geojson = requests.get(BUILDING_GEOJSON_URL).json()
    return (pd.DataFrame(f['properties'] for f in geojson['features'])
              .rename(columns={'BldFAMIS_N': 'name', 'centroid': 'coords', 'BldWebPage': 'url'})
              .set_index('Building_A')
              [['name', 'coords', 'url']])


def dump_df_as_db(df):
    with sqlite3.connect('buildings') as db:
        df = (df.reset_index()
                .rename(columns={'index': 'abbr'})
                .reset_index()
                .rename(columns={'index': '_id'}))
        df['suggest_intent_data'] = df['suggest_text_1'] = df['abbr']
        df['suggest_text_2'] = df['name']
        df.to_sql('buildings', db, if_exists='replace', index=False)


def main():
    names = fetch_nice_building_names()
    locations = fetch_building_locations()
    df = names.join(locations, how='outer')
    df = df.fillna('')
    for row in df.itertuples(index=True):
        if not row.pretty_name:
            df.loc[row.Index, 'pretty_name'] = row.name.title()
        if not row.coords:
            url = BUILDING_INFO_URL.format(row.Index)
            df.loc[row.Index, 'url'] = url
            building_html = requests.get(url).text
            match = re.search('google.maps.LatLng\((.*)\)', building_html)
            if match:
                df.loc[row.Index, 'coords'] = match.group(1).strip()
            else:
                print(f"Couldn't find coords for {row.Index}")
    df[['lat', 'lng']] = df.coords.str.split(',', expand=True).astype('float')
    df['name'] = df['pretty_name']
    df = df.drop(['coords', 'pretty_name'], axis='columns')
    dump_df_as_db(df)


if __name__ == '__main__':
    main()
