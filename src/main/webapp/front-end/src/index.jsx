import React from 'react';
import ReactDOM from 'react-dom';
import {Router, Route, hashHistory} from 'react-router';
import {createStore} from 'redux';
import {Provider} from 'react-redux';
import reducer from './reducer';
import {setState} from './action_creators';
import App from './components/App';
import {CityListContainer} from './components/CityList';
import {ActivityListContainer} from './components/ActivityList';
import {List} from 'immutable';

import {fetchAllCities} from './ajax_helpers';

const cities = List.of({"name": "Beijing", "id": 1, "photo": "https://media-cdn.tripadvisor.com/media/photo-s/03/9b/2d/b2/beijing.jpg"},
                {"name": "New York", "id": 2, "photo": "https://media-cdn.tripadvisor.com/media/photo-s/03/9b/2d/f2/new-york-city.jpg"});

const store = createStore(reducer);
store.dispatch(setState({
    cities: cities
  })
);

//fetchAllCities();

const routes = <Route component={App}>
  <Route path="/" component={CityListContainer} />
  <Route path="/cities/:cityId/activities" component={ActivityListContainer} />
</Route>;

ReactDOM.render(
  <Provider store={store}>
    <Router history={hashHistory}>{routes}</Router>
  </Provider>,
  document.getElementById('app')
);