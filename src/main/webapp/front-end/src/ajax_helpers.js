import $ from 'jquery';

const fullstack = false;
const domain = fullstack ? "" : "http://localhost:8080"


export function fetchAllCities(){

    $.ajax({
        url: domain+'/cities',
        type: 'GET',
        dataType: 'json',
        success: function(data) {
              alert("THIS MANY CITIES: "+data.length);
              return data;
        }
    });
}

export function fetchActivitiesByCityId(cityId){
    $.ajax({
        url: domain+'/cities/'+cityId+'/activities',
        type: 'GET',
        dataType: 'json',
        success: function(data){
            alert("THIS MANY ACTIVITIES: "+data.length);
            return data;
        }
    });
}

