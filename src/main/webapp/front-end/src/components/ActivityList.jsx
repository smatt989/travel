import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import {connect} from 'react-redux';
import Activity from './Activity';

import * as actionCreators from '../action_creators';
//import {selectCity} from '../action_creators';

export const ActivityList = React.createClass({
  mixins: [PureRenderMixin],
  getActivities: function() {
    return this.props.activities || [];
  },
  getCityId: function() {
    return this.props.params.cityId || null;
  },
  componentDidMount: function() {
     this.props.selectCity(this.getCityId());
  },
  render: function() {
      return <div className="activity-list">
        {this.getActivities().map(activity =>
            <Activity name={activity.name} id={activity.id} overallScore={activity.overallScore} />
        )}
      </div>;
  }
});

function mapStateToProps(state) {
  return {
    activities: state.get('activities')
  };
}

export const ActivityListContainer = connect(mapStateToProps, actionCreators)(ActivityList);