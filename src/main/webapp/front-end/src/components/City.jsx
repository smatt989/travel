import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';

const hashHistory = require('react-router').hashHistory;

export default React.createClass({
  mixins: [PureRenderMixin],
  getCity: function() {
    return this.props.city || null;
  },
  getName: function() {
    return this.getCity().name || "";
  },
  getId: function() {
    return this.getCity().id || null;
  },
  getPhoto: function() {
    return this.getCity().photo || "";
  },
  click: function() {
     hashHistory.push("/cities/"+this.getId()+"/activities");
  },
  render: function() {
    return <div className="city" onClick={this.click}>
        <h1>{this.getName()}</h1>
        <img src={this.getPhoto()} />
     </div>;
  }
});