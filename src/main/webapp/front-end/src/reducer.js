import {Map, List} from 'immutable';
import {fetchActivitiesByCityId} from './ajax_helpers';


const beijingActivities = List.of(
    {"name": "forbidden city", "id": 1, "overallScore": 8.9},
    {"name": "great wall", "id": 2, "overallScore": 7.3}
);

function setState(state, newState) {
    return state.merge(newState);
}

function selectCity(state, city) {
    return state.set('selectedCity', city);
}

function resetCity(state) {
    return state.remove('selectedCity');
}

function setActivities(state, activities) {
    return state.set('activities', activities)
}

function resetActivities(state) {
    return state.remove('activities');
}

export default function(state = Map(), action) {
  switch (action.type) {
  case 'SET_STATE':
    return setState(state, action.state);
  case 'SELECT_CITY':
    return setActivities(resetActivities(selectCity(resetCity(state), action.city)), fetchActivitiesByCityId(action.city));
  }
  return state;
}