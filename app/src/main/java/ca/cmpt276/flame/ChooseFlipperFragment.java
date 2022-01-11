package ca.cmpt276.flame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.cmpt276.flame.model.Child;
import ca.cmpt276.flame.model.FlipManager;

/**
 * A fragment class to pop up dialog box to enable user to choose child to go next
 */
public class ChooseFlipperFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private FlipManager flipManager;
    private List<Child> childList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_choose_flipper, container, false);
        setupButtons();

        recyclerView = view.findViewById(R.id.chooseFlipper_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ChildViewAdapter recyclerAdapter = new ChildViewAdapter(getContext(), childList);
        recyclerView.setAdapter(recyclerAdapter);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        flipManager = FlipManager.getInstance();
        childList = flipManager.getTurnQueue();
    }

    private void setupButtons() {
        Button cancelBtn = view.findViewById(R.id.chooseFlipper_btnSelectCanceled);
        cancelBtn.setOnClickListener(v -> getFragmentManager().popBackStack());

        Button noOneBtn = view.findViewById(R.id.chooseFlipper_btnSelectNoOne);
        noOneBtn.setOnClickListener(v -> {
            flipManager.overrideTurnChild(null);
            ((FlipCoinActivity) getActivity()).updateUI();
            getFragmentManager().popBackStack();
        });
    }

    private static class ChildViewAdapter extends RecyclerView.Adapter<ChildViewAdapter.ViewHolder> {
        private final FlipManager flipManager = FlipManager.getInstance();
        private final Context context;
        private final List<Child> childList;

        ChildViewAdapter(Context context, List<Child> childList) {
            this.context = context;
            this.childList = childList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.item_child, parent, false);
            ChildViewAdapter.ViewHolder vHolder = new ChildViewAdapter.ViewHolder(v);

            vHolder.childItem.setOnClickListener(v1 -> {
                flipManager.overrideTurnChild(vHolder.childObj);
                ((FlipCoinActivity) context).updateUI();
                ((AppCompatActivity) context).getSupportFragmentManager().popBackStack();
            });

            return vHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder vHolder, int position) {
            vHolder.childObj = childList.get(position);
            vHolder.childImage.setImageBitmap(vHolder.childObj.getImageBitmap(context));
            vHolder.childName.setText(vHolder.childObj.getName());
            vHolder.childOrderInQ.setText(context.getString(R.string.child_pos_in_queue, position + 1));
        }

        @Override
        public int getItemCount() {
            return childList.size();
        }

        /**
         * A view holder class responsible for filling the view of each item in the data set
         */
        private static class ViewHolder extends RecyclerView.ViewHolder {
            private final LinearLayout childItem;
            private final ImageView childImage;
            private final TextView childName;
            private final TextView childOrderInQ;
            private Child childObj;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                childItem = itemView.findViewById(R.id.item_child);
                childImage = itemView.findViewById(R.id.item_child_image);
                childName = itemView.findViewById(R.id.item_child_name);
                childOrderInQ = itemView.findViewById(R.id.item_child_order);
            }
        }
    }
}