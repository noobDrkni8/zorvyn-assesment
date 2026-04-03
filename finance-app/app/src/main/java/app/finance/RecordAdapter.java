package app.finance;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import app.finance.models.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private List<Record> records = new ArrayList<>();

    public void setRecords(List<Record> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = records.get(position);
        holder.tvCategory.setText(record.getCategory());
        holder.tvDate.setText(record.getDate());

        boolean isIncome = "income".equals(record.getType());
        String prefix = isIncome ? "+" : "-";
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%s$%.2f", prefix, record.getAmount()));
        holder.tvAmount.setTextColor(isIncome ? 0xFF03DAC5 : 0xFFCF6679);
        holder.viewIndicator.setBackgroundColor(isIncome ? 0xFF03DAC5 : 0xFFCF6679);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount;
        View viewIndicator;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tv_record_category);
            tvDate = itemView.findViewById(R.id.tv_record_date);
            tvAmount = itemView.findViewById(R.id.tv_record_amount);
            viewIndicator = itemView.findViewById(R.id.view_type_indicator);
        }
    }
}