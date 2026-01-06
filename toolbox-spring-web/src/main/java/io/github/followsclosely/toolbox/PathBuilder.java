package io.github.followsclosely.toolbox;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathBuilder {

    private final ArrayDeque<String> path = new ArrayDeque<>();

    public PathBuilder add(String path){
        this.path.add(path);
        return this;
    }

    public PathBuilder explode(String value) {
        if( value != null ) {
            if (value.contains("-")) {
                return explodeNumber(value);
            } else {
                return explodeStringNumber(value);
            }
        }

        return this;
    }

    public PathBuilder explodeNumber(String number) {
        try {
            int cleaned = Integer.parseInt((number.contains("-")) ? number.split("-")[0] : number);
            for(int max=100_000; max>=100 ; max = max/10){
                int value = (cleaned / max) * max;
                path.add(String.valueOf( ( value == 0 ) ? max : value));
            }
            path.add(number);
        } catch (NumberFormatException e) {
            path.add("other");
            path.add(number);
        }

        return this;
    }

    public PathBuilder explodeStringNumber(String value) {
        try {
            Pattern pattern = Pattern.compile("^[a-zA-Z]+\\d+$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                path.add(matcher.group(0));
                path.add(matcher.group(1));
            } else {
                path.add("other2");
                path.add(value);
            }
        } catch (Exception e) {
            path.add("other");
            path.add(value);
        }

        return this;
    }

    public PathBuilder explodeOnGroups(String value, String regex) {
        try {
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                //for each group add to path
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    path.add(matcher.group(i));
                }
            } else {
                path.add("regex_no_match");
                path.add(value);
            }
        } catch (Exception e) {
            path.add("regex_other");
            path.add(value);
        }

        return this;
    }

    public Collection<String> build(){
        return path;
    }
    public String[] toArray(){
        return path.toArray(new String[0]);
    }

    public PathBuilder reset(){
        this.path.clear();
        return this;
    }
}
