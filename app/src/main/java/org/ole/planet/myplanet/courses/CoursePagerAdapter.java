package org.ole.planet.myplanet.courses;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class CoursePagerAdapter extends FragmentStatePagerAdapter {

    private String[] steps;
    private String courseId;

    public CoursePagerAdapter(FragmentManager fm, String courseId, String[] steps) {
        super(fm);
        this.steps = steps;
        this.courseId = courseId;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle b = new Bundle();
        Fragment f;
        if (position == 0) {
            f = new CourseDetailFragment();
            b.putString("courseId", courseId);

        } else {
            f = new CourseStepFragment();
            b.putString("stepId", steps[position - 1]);
        }
        f.setArguments(b);
        return f;
    }

    @Override
    public int getCount() {
        return steps.length + 1;
    }
}
