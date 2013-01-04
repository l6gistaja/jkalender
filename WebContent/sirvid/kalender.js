function r(name, width, filename) {
    window.open (
        //"r.php?f=" + filename + "&w=" + width + "&n=" +name,
		"svg/" + filename,
        "runewindow",
        "height=220,width=" + (20+width)
            + ",status=0,toolbar=0,location=0,menubar=0,directories=0,resizable=1"
    );
    
    return false;
}