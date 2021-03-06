package com.tutortrack.dialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tutortrack.R;
import com.tutortrack.api.API;
import com.tutortrack.api.Subject;
import com.tutortrack.api.student.StudentAppointment;
import com.tutortrack.api.student.TutorBlock;
import com.tutortrack.control.RangeTimePicker;

public class AppointmentCreator extends Activity {

	public class MakeAppointmentTask extends AsyncTask<Void, Void, Void> {

		private Context _context;
		private ProgressDialog d;
		private StudentAppointment appt;

		public MakeAppointmentTask(Context c) {
			_context = c;
			d = ProgressDialog.show(_context, "", "Making appointment...",
					true, false);
		}

		public void onPreExecute() {
			d.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			appt = API.getInstance().makeAppointmentWithTutor(block.getTutor(),
					apptDate, apptTime, block.getWhere(), getCheckedSubjects());
			return null;
		}

		public void onPostExecute(Void result) {
			d.cancel();
			if (appt != null) {
				Intent i = new Intent();
				i.putExtra("StudentAppointment", appt);
				setResult(RESULT_OK, i);
				System.out.println("Result OK");
			} else {
				setResult(RESULT_CANCELED);
				System.out.println("Result Canceled");
			}
			finish();
		}

	}

	private TutorBlock block;
	private TextView tutorNameLbl;
	private LinearLayout subjectCheckBoxLayout;
	private DatePicker dateCalendar;
	private RangeTimePicker timePicker;
	private Button ok, cancel;
	private ArrayList<CheckBox> subjectCheckBoxes = new ArrayList<CheckBox>();
	private ArrayList<Subject> subj;

	private Calendar apptDate;
	private Calendar apptTime;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.make_appointment_dialog);

		block = (TutorBlock) getIntent().getSerializableExtra("data");
		tutorNameLbl = (TextView) findViewById(R.id.tutorNameLbl);
		tutorNameLbl.setText(block.getTutor().getName());

		subjectCheckBoxLayout = (LinearLayout) findViewById(R.id.subjectCheckBoxLayout);
		dateCalendar = (DatePicker) findViewById(R.id.datePicker);

		dateCalendar.setMinDate(block.getStartDate().getTimeInMillis());
		dateCalendar.setMaxDate(block.getEndDate().getTimeInMillis());
		dateCalendar.setCalendarViewShown(true);
		dateCalendar.setSpinnersShown(false);

		timePicker = (RangeTimePicker) findViewById(R.id.timePicker);
		timePicker.setIs24HourView(false);
		timePicker.setMin(block.getStartTime().get(Calendar.HOUR_OF_DAY), block.getStartTime().get(Calendar.MINUTE));
		timePicker.setMax(block.getEndTime().get(Calendar.HOUR_OF_DAY), block.getEndTime().get(Calendar.MINUTE));

		apptDate = new GregorianCalendar();
		apptTime = new GregorianCalendar();

		for (Subject s : block.getSubjects()) {
			CheckBox feature1 = new CheckBox(this);
			feature1.setText(s.toString());
			subjectCheckBoxes.add(feature1);
			subjectCheckBoxLayout.addView(feature1);
		}

		cancel = (Button) findViewById(R.id.button_cancel);
		ok = (Button) findViewById(R.id.button_create);

		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();

			}
		});

		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				apptDate.set(dateCalendar.getYear(), dateCalendar.getMonth()+1,
						dateCalendar.getDayOfMonth());
				apptTime.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
				apptTime.set(Calendar.MINUTE, timePicker.getCurrentMinute());
				subj = new ArrayList<Subject>();

				for (int i = 0; i < subjectCheckBoxes.size(); ++i) {
					CheckBox b = subjectCheckBoxes.get(i);
					if (b.isChecked()) {
						subj.add(block.getSubjects().get(i));
					}
				}
				
				if (subj.size() > 0)
					new MakeAppointmentTask(AppointmentCreator.this).execute();
				else
					Toast.makeText(AppointmentCreator.this, "No subjects selected", Toast.LENGTH_SHORT).show();

			}
		});

	}

	private ArrayList<Subject> getCheckedSubjects() {

		ArrayList<Subject> subjects = new ArrayList<Subject>();

		for (CheckBox c : this.subjectCheckBoxes) {
			if (c.isChecked()) {
				subjects.add(new Subject((String) c.getText()));
			}
		}

		return subjects;
	}

}
