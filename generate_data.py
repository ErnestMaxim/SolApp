import json
import random
import argparse
from datetime import datetime, timedelta

# Define possible weather statuses
CONDITIONS = ["SUNNY", "CLOUDY", "RAINY", "SNOWY", "UNKNOWN"]

# Function to generate fake countries
def generate_countries(num_countries):
    countries = []
    for i in range(1, num_countries + 1):
        country = {
            "id": i,
            "country_name": f"Country_{i}",
            "country_iso": f"ISO_{i:03}"
        }
        countries.append(country)
    return countries

# Function to generate fake cities
def generate_cities(countries, num_cities):
    cities = []
    for i in range(1, num_cities + 1):
        country = random.choice(countries)
        city = {
            "id": i,
            "city_name": f"City_{i}",
            "latitude": round(random.uniform(-90.0, 90.0), 4),
            "longitude": round(random.uniform(-180.0, 180.0), 4),
            "country_id": country["id"]
        }
        cities.append(city)
    return cities

# Function to generate weather statuses
def generate_weather_statuses():
    weather_statuses = []
    for i, condition in enumerate(CONDITIONS, start=1):
        status = {
            "id": i,
            "weather_status": condition,
            "description": f"Weather condition: {condition}"
        }
        weather_statuses.append(status)
    return weather_statuses

# Function to generate fake weather data
def generate_weather_data(cities, weather_statuses, num_days):
    weather_data = []
    for city in cities:
        for day in range(num_days):
            date = datetime.today() - timedelta(days=day)
            status = random.choice(weather_statuses)
            weather = {
                "id": random.randint(1000, 9999),
                "city_id": city["id"],
                "forecast_date": date.strftime("%Y-%m-%d"),
                "max_temperature": random.randint(0, 40),
                "min_temperature": random.randint(-10, 25),
                "weather_status_id": status["id"]
            }
            weather_data.append(weather)
    return weather_data

def main():
    # Command-line argument parsing
    parser = argparse.ArgumentParser(description="Generate fake weather data for database.")
    parser.add_argument("-c", "--countries", type=int, default=5, help="Number of countries to generate")
    parser.add_argument("-i", "--cities", type=int, default=10, help="Number of cities to generate")
    parser.add_argument("-d", "--days", type=int, default=7, help="Number of days of weather data per city")
    parser.add_argument("-o", "--output", type=str, default="sol.json", help="Output JSON file name")

    args = parser.parse_args()

    # Generate data
    countries = generate_countries(args.countries)
    cities = generate_cities(countries, args.cities)
    weather_statuses = generate_weather_statuses()
    weather_data = generate_weather_data(cities, weather_statuses, args.days)

    # Combine into a JSON structure
    data = {
        "countries": countries,
        "cities": cities,
        "weather_statuses": weather_statuses,
        "weather_daily_forecast": weather_data
    }

    # Save to a JSON file
    with open(args.output, "w") as json_file:
        json.dump(data, json_file, indent=4)

    print(f"Fake weather and location data generated successfully! Check '{args.output}'.")

if __name__ == "__main__":
    main()
