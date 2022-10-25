package com.example.pochtao.controllers
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pochtao.R
import com.example.pochtao.beans.Email
import java.util.*


class EmailsListAdapter(
                        activity: Activity,
                        private val data: ArrayList<Email>
): RecyclerView.Adapter<EmailsListAdapter.VH>() {

    var ctx = activity
    var emailsController : EmailController = EmailController()

    inner class VH(itemView: View):RecyclerView.ViewHolder(itemView){
        var content: TextView
        var from: TextView
        var subject: TextView
        var card: CardView

        init {
            content= itemView.findViewById(R.id.email_content)
            from= itemView.findViewById(R.id.email_from)
            subject= itemView.findViewById(R.id.email_subject)
            card = itemView.findViewById(R.id.cardview)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }
    private val inflater : LayoutInflater

    init {
        this.inflater = activity.layoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = inflater.inflate(R.layout.list_item_email, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        //To set data in the item view
        vh.content.text = data.get(position).body
        vh.subject.text = data.get(position).subject
        vh.from.text = data.get(position).from

        vh.card.setCardBackgroundColor(Color.WHITE);
        vh.from.setTypeface(Typeface.DEFAULT_BOLD)
        vh.subject.setTypeface(Typeface.DEFAULT_BOLD)
        //TODO check is openIf is already read
        if(data.get(position).read){
            //To update front if email have already have been read
            vh.card.setCardBackgroundColor(Color.WHITE);
            vh.from.setTypeface(Typeface.DEFAULT)
            vh.subject.setTypeface(Typeface.DEFAULT)
        }

        vh.card.setOnClickListener{
            //To redirect to EmailDetails (activity)
            val intent = Intent(ctx, EmailDetails::class.java)
            intent.putExtra("from", data.get(position).from)
            intent.putExtra("content", data.get(position).body)
            intent.putExtra("subject", data.get(position).subject)
            ctx.startActivity(intent)
        }
    }


}
