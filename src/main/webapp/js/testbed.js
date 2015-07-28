function sendAjax() {

    // get inputs
    var measurement = new Object();
    measurement.json = $('#json').val();

    $.ajax({
        url: "jsonservlet",
        type: 'POST',
        dataType: 'json',
        data: JSON.stringify(measurement),
        contentType: 'application/json',
        mimeType: 'application/json',

        success: function (data) {
            $("tr:has(td)").remove();

            $.each(data, function (index, article) {

                var td_categories = $("<td/>");
                $.each(measurement.categories, function (i, tag) {
                    var span = $("<span class='label label-info' style='margin:4px;padding:4px' />");
                    span.text(tag);
                    td_categories.append(span);
                });

                var td_tags = $("<td/>");
                $.each(measurement.tags, function (i, tag) {
                    var span = $("<span class='label' style='margin:4px;padding:4px' />");
                    span.text(tag);
                    td_tags.append(span);
                });

                $("#added-measurements").append($('<tr/>')
                        .append($('<td/>').html("<a href='"+measurement+"'>"+measurement.json+"</a>"))
                        .append(td_categories)
                        .append(td_tags)
                );

            });
        },
        error:function(data,status,er) {
            alert("error: "+data+" status: "+status+" er:"+er);
        }
    });
}

var map;
var ajaxRequest;
var plotlist;
var twoglayer;
var threeglayer;
var fourglayer;
var currentOperator;

function onMapMove(e) {
    var geojsonFeature =
 {"type":"Polygon","coordinates":[[[-8.71506546218995,41.3154391666556],[-8.71506546227884,41.3154391659974],
     [-8.71506546253635,41.3154391653648],[-8.71506546295258,41.3154391647823],[-8.71506546351155,41.3154391642722],
     [-8.71506546419176,41.3154391638542],[-8.71506546496708,41.3154391635442],[-8.71506546580772,41.3154391633542],
     [-8.71506546668136,41.3154391632915],[-8.71506546755445,41.3154391633585],[-8.71506546839341,41.3154391635527],
     [-8.71506546916602,41.3154391638665],[-8.71506546984257,41.3154391642879],[-8.71506547039708,41.3154391648007],
     [-8.71506547080823,41.3154391653853],[-8.71506547106023,41.3154391660191],[-8.71506547114338,41.3154391666778],
     [-8.71506547105449,41.315439167336],[-8.71506547079699,41.3154391679685],[-8.71506547038075,41.315439168551],
     [-8.71506546982179,41.3154391690611],[-8.71506546914157,41.3154391694792],[-8.71506546836625,41.3154391697892],
     [-8.71506546752561,41.3154391699792],[-8.71506546665197,41.3154391700419],[-8.71506546577889,41.3154391699748],
     [-8.71506546493992,41.3154391697807],[-8.71506546416732,41.3154391694669],[-8.71506546349076,41.3154391690455],
     [-8.71506546293625,41.3154391685326],[-8.7150654625251,41.3154391679481],[-8.7150654622731,41.3154391673143],[-8.71506546218995,41.3154391666556]]]};



    L.geoJson(geojsonFeature,
        {style:
        {color: "#00ff00"}
        })
        .addTo(map);
}

function markerOptions(feature, latlng) {
    return L.circleMarker(latlng, geojsonMarkerOptions);
}

var geojsonMarkerOptions = {
    radius: 8,
    fillColor: "#ff7800",
    color: "#000",
    weight: 1,
    opacity: 1,
    fillOpacity: 0.8
};


function initmap() {
    // set up the map
    map = L.map('map');

    map.locate({setView: true, maxZoom: 16});
    map.on('locationerror', onLocationError);
    map.on('locationfound', onLocationFound);
}

function onLocationFound(e){
    var mqLayer = L.tileLayer("http://otile{s}.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png", {
        subdomains: "1234",
        attribution: "&copy; <a href='http://www.openstreetmap.org/'>OpenStreetMap</a> and contributors, under an <a href='http://www.openstreetmap.org/copyright' title='ODbL'>open license</a>. Tiles Courtesy of <a href='http://www.mapquest.com/'>MapQuest</a> <img src='http://developer.mapquest.com/content/osm/mq_logo.png'>"
    });
    mqLayer.addTo(map);

    map.on('moveend', onMapMove);

}

function onLocationError(e) {
    alert(e.message);
    map.setView([41.147627, -8.567756], 13);
    onLocationFound();
}