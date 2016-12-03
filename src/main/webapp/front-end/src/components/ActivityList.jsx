import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import {connect} from 'react-redux';
import Activity from './Activity';

import {selectCity, fetchActivities, fetchActivitiesSuccess, fetchActivitiesError} from '../action_creators';

export const ActivityList = React.createClass({
  mixins: [PureRenderMixin],
  getError: function () {
    return this.props.activityList.error || null;
  },
  getIsLoading: function() {
    return this.props.activityList.loading || false;
  },
  getActivities: function() {
    return this.props.activityList.activities || [];
  },
  getCityId: function() {
    return this.props.params.cityId || null;
  },
  componentDidMount: function() {
    this.props.selectCity(this.getCityId());
    this.props.fetchActivities(this.getCityId());
  },
  render: function() {
      if(this.getIsLoading()) {
        return <div className="container"><h1>Posts</h1><h3>Loading...</h3></div>
      } else if(this.getError()) {
        return <div className="alert alert-danger">Error: {error.message}</div>
      }

      return <div className="activity-list">
        {this.getActivities().map(activity =>
            <Activity name={activity.name} id={activity.id} overallScore={activity.funRating} />
        )}
      </div>;
  }
});

function mapStateToProps(state) {
  return {
    activityList: state.get('activityList')
  };
}

const mapDispatchToProps = (dispatch) => {
    return {
        selectCity: (cityId) => {
            dispatch(selectCity(cityId))
        },
        fetchActivities: (cityId) => {
            dispatch(fetchActivities(cityId)).then((response) => {
               !response.error ? dispatch(fetchActivitiesSuccess(response.payload.data)) : dispatch(fetchActivitiesError(response.payload.data));
           });
        }
    }
}

export const ActivityListContainer = connect(mapStateToProps, mapDispatchToProps)(ActivityList);