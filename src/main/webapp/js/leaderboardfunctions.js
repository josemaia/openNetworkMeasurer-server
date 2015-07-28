function loadtable(){
    var items = "",
        url = "leaderboard";

    $.ajax({
        url: url,
        cache: false,
        type: 'POST',
        contentType: 'application/json',
        mimeType: 'application/json',
        dataType: "json",
        beforeSend: function() {
            $("h2").html("Loading...");
        },
        error: function() {
            $("h2").html("Error loading leaderboard data");
        },
        success: function(result) {
            if(result[0].err){
                $("h2").html(result[0].err);
            }
            else{
                for(var i = 0; i<result.length; i++){
                    items += "<tr>";
                    items += "<td>" + result[i].id + "</td>";
                    items += "<td>" + result[i].measurements + "</td>";
                    items += "</tr>";
                }
                $("#mytable tbody").html(items);

                $("h2").html("");
            }
        }
    });
}