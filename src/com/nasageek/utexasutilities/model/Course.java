
package com.nasageek.utexasutilities.model;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public abstract class Course implements Parcelable, Serializable, Comparable<Course> {

    private static final long serialVersionUID = 1L;
    protected String id;
    protected String name;
    protected String course_code;
    protected Date start_at;
    protected String type;
    protected String term_name;
    private static final String COURSE_CODE_REGEX = "([A-Z] ?){0,3} \\d+[A-Z]?";
    protected static final String TERM_NAME_REGEX = "\\d{4} (Spring|Fall|Summer)";

    public String getCourseCode() {
        return course_code;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTermName() {
        return term_name;
    }

    public boolean isTermNameValid() {
        return getTermName().matches(TERM_NAME_REGEX);
    }

    /**
     * Sorts courses chronologically first, then by course code (UGS 302, EE 316, etc)
     * @param other Course to compare to
     * @return -1 if this comes before other
     *         0 if their terms and course codes are the same,
     *         1 if this comes after other
     */
    @Override
    public int compareTo(Course other) {
        // shove courses with bad term names to the bottom
        if (!this.isTermNameValid()) {
            return 1;
        }
        if (!other.isTermNameValid()) {
            return -1;
        }
        String rTermName = this.getTermName();
        String lTermName = other.getTermName();
        Integer lyear = Integer.parseInt(lTermName.split(" ")[0]);
        Integer ryear = Integer.parseInt(rTermName.split(" ")[0]);
        if (!lyear.equals(ryear)) {
            return lyear.compareTo(ryear);
        }
        String lsemester = lTermName.split(" ")[1];
        String rsemester = rTermName.split(" ")[1];
        if (!lsemester.equals(rsemester)) {
            // 3 possibilities, Spring, Summer, Fall
            switch (lsemester) {
                case "Spring": return -1;
                case "Summer": return rsemester.equals("Spring") ? 1 : -1;
                case "Fall": return 1;
            }
        }
        // shove courses with bad course codes to the bottom
        if (!this.getCourseCode().matches(COURSE_CODE_REGEX)) {
            return 1;
        }
        if (!other.getCourseCode().matches(COURSE_CODE_REGEX)) {
            return -1;
        }
        return(this.getCourseCode().compareTo(other.getCourseCode()));
    }
}
