import axios from 'axios';

const fullstack = false;
const domain = fullstack ? "" : "http://localhost:8080";

export function setState(state) {
    return {
        type: 'SET_STATE',
        state: state
    };
}

export function selectCity(city) {
    return {
        type: 'SELECT_CITY',
        city: city
    };
}

export function fetchCities(){
    const request = axios({
        method: 'get',
        url: `${domain}/cities`,
        header: []
    });

    return {
        type: 'FETCH_CITIES',
        payload: request
    }
}

export function fetchCitiesSuccess(cities){
    return {
        type: 'FETCH_CITIES_SUCCESS',
        payload: cities
    }
}

export function fetchCitiesError(error){
    return {
        type: 'FETCH_CITIES_SUCCESS',
        payload: error
    }
}

export function fetchActivities(cityId){
    const request = axios({
        method: 'get',
        url: `${domain}/cities/${cityId}/activities`,
        headers: []
      });

    return {
        type: 'FETCH_ACTIVITIES',
        payload: request
    }
}

export function fetchActivitiesSuccess(activities){
    return {
        type: 'FETCH_ACTIVITIES_SUCCESS',
        payload: activities
    }
}

export function fetchActivitiesError(error){
    return {
        type: 'FETCH_ACTIVITIES_ERROR',
        payload: error
    }
}