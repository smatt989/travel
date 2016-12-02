import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';

export default React.createClass({
  mixins: [PureRenderMixin],
  getName: function() {
    return this.props.name || "";
  },
  getId: function() {
    return this.props.id || null;
  },
  getOverallScore: function() {
    return this.props.overallScore || null;
  },
  render: function() {
    return <div className="activity">
        <h1>{this.getName()}</h1>
        <p>({this.getOverallScore()})</p>
     </div>;
  }
});