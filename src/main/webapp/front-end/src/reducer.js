import {Map, List} from 'immutable';

function setState(state, newState) {
    return state.merge(newState);
}

function setCity(state, city) {
    return state.set('city', city);
}

function fetchActivities(state) {
    return state.set('activityList', {activities:[], error: null, loading: true});
}

function fetchActivitiesSuccess(state, activities) {
    return state.set('activityList', {activities: activities, error: null, loading: false});
}

function fetchActivitiesError(state, error) {
    return state.set('activityList', {activities: [], error: error, loading: false});
}

function fetchCities(state) {
    return state.set('cityList', {cities: [], error: null, loading: true});
}

function fetchCitiesSuccess(state, cities) {
    return state.set('cityList', {cities: cities, error: null, loading: false});
}

function fetchCitiesError(state, error) {
    return state.set('cityList', {cities: [], error: error, loading: false});
}

export default function(state = Map(), action) {
  switch (action.type) {
  case 'SET_STATE':
    return setState(state, action.state);
  case 'SELECT_CITY':
    return setCity(state, action.city);
  case 'FETCH_ACTIVITIES':
    return fetchActivities(state);
  case 'FETCH_ACTIVITIES_SUCCESS':
    return fetchActivitiesSuccess(state, action.payload);
  case 'FETCH_ACTIVITIES_ERROR':
    return fetchActivitiesError(state, action.payload);
  case 'FETCH_CITIES':
    return fetchCities(state);
  case 'FETCH_CITIES_SUCCESS':
    return fetchCitiesSuccess(state, action.payload);
  case 'FETCH_CITIES_ERROR':
    return fetchCitiesError(state, action.payload);
  }
  return state;
}