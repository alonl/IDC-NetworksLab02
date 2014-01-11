$('#test').click(function () {
    if ($('#test').html() === "Switch to Premium Mode") {
        $("#reminders").attr("href", "/reminders.html?premium=yeah!");
        $("#tasks").attr("href", "/tasks.html?premium=sure!");
        $("#polls").attr("href", "/polls.html?premium=awesome!");
        
        $('#test').text("Switch to Basic mode");
    } else {
        $("#reminders").attr("href", "/reminders.html");
        $("#tasks").attr("href", "/tasks.html");
        $("#polls").attr("href", "/polls.html");
        
        $('#test').text("Switch to Premium Mode");
    }
});