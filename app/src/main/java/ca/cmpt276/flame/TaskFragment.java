package ca.cmpt276.flame;


import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import ca.cmpt276.flame.model.Task;
import ca.cmpt276.flame.model.TaskManager;

/**
 * TaskFragment: set up a custom DialogFragment layout to show detail information
 * of clicked task including the Name of task, the Description, who is turn child.
 * Allow user to confirm the turn child.
 * Right top image button allow user to edit task in TaskEditActivity
 */

public class TaskFragment extends AppCompatDialogFragment {
    private final TaskManager taskManager = TaskManager.getInstance();
    private final TaskActivity taskActivity;
    private final Task clickedTask;

    public TaskFragment(TaskActivity taskActivity, Task clickedTask) {
        this.taskActivity = taskActivity;
        this.clickedTask = clickedTask;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_task_desc, null);

        fillViews(dialogLayout);
        setupButtons(dialogLayout);

        return new AlertDialog.Builder(getActivity())
                .setView(dialogLayout)
                .create();
    }

    private void fillViews(View dialogLayout) {
        TextView txtTaskName = dialogLayout.findViewById(R.id.task_dialog_txtName);
        txtTaskName.setText(clickedTask.getName());

        if(clickedTask.getNextChild() != null) {
            TextView txtChildName = dialogLayout.findViewById(R.id.task_dialog_txtTurnChild);
            txtChildName.setText(getString(R.string.task_turn_child, clickedTask.getNextChild().getName()));

            ImageView imagePortrait = dialogLayout.findViewById(R.id.task_dialog_imagePortrait);
            imagePortrait.setImageBitmap(clickedTask.getNextChild().getImageBitmap(this.getContext()));
        }


        TextView txtTaskDesc = dialogLayout.findViewById(R.id.task_dialog_txtDescription);
        txtTaskDesc.setText(clickedTask.getDesc());


    }

    private void setupButtons(View dialogLayout) {
        Button btnTookTurn = dialogLayout.findViewById(R.id.task_dialog_btnTookTurn);
        if(clickedTask.getNextChild() != null) {
            btnTookTurn.setVisibility(View.VISIBLE);
            btnTookTurn.setOnClickListener(v -> {
                taskManager.takeTurn(clickedTask);
                taskActivity.refreshListView();
                this.dismiss();
            });
        }

        Button btnCancel = dialogLayout.findViewById(R.id.task_dialog_btnCancel);
        btnCancel.setOnClickListener(v -> {
            this.dismiss();
        });

        ImageButton btnEdit = dialogLayout.findViewById(R.id.task_dialog_imageBtnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = TaskEditActivity.makeIntent(getActivity(), clickedTask);
            startActivity(intent);
            this.dismiss();
        });
    }
}
