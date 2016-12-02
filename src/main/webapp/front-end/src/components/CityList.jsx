import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import {connect} from 'react-redux';
import City from './City';
import * as actionCreators from '../action_creators';

export const CityList = React.createClass({
  mixins: [PureRenderMixin],
  getCities: function() {
    return this.props.cities || [];
  },
  render: function() {
      return <div className="city-list">
        {this.getCities().map(city =>
            <City city={city} {...this.props} />
        )}
      </div>;
  }
});

function mapStateToProps(state) {
  return {
    cities: state.get('cities')
  };
}

export const CityListContainer = connect(mapStateToProps, actionCreators)(CityList);