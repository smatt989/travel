export function setState(state) {
  return {
    type: 'SET_STATE',
    state
  };
}

export function selectCity(city) {
  return {
    type: 'SELECT_CITY',
    city
  };
}