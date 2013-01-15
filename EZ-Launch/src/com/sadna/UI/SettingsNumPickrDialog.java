package com.sadna.UI;

import com.sadna.data.DataManager;
import com.sadna.widgets.application.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class SettingsNumPickrDialog extends DialogFragment{

	private DataManager dm;
	private NumberPicker numberPK;
	private Activity activity;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	activity = getActivity();
        // Use the Builder class for convenient dialog construction
    	LayoutInflater inflater = activity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.appsPerScreenDescription)
        		.setView(inflater.inflate(R.layout.settings_num_pickr, null))
        		.setPositiveButton(R.string.save_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   dm.setApplicationLimit(numberPK.getValue());
                   }
               })
               .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        
        // Create data manager
         dm = new DataManager(activity.getBaseContext());
        
		// numberPicker
		numberPK = (NumberPicker) getView().findViewById(R.id.numberPicker);
		numberPK.setMaxValue(50);
		numberPK.setMinValue(1);
		numberPK.setWrapSelectorWheel(true);
		numberPK.setValue(dm.getApplicationLimit());
		
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		
//		final DataManager dm = new DataManager(this.getBaseContext());
//		
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.settings_num_pickr);
//		
//		// numberPicker
//		final NumberPicker numberPK1 = (NumberPicker) findViewById(R.id.numberPicker);
//		numberPK1.setMaxValue(50);
//		numberPK1.setMinValue(1);
//		numberPK1.setWrapSelectorWheel(true);
//		numberPK1.setValue(dm.getApplicationLimit());
//		
//		
//        // button
//        final Button btn1 = (Button) findViewById(R.id.button);
//        // Perform action on click
//        btn1.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {  
//            	 
//            	dm.setApplicationLimit(numberPK1.getValue());
//            	finish();
//            }
//        });
//		
//	}

