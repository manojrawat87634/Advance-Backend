import React, { useState } from "react";
import {
  Calendar,
  Trash2,
  Edit3,
  Tag,
  CheckCircle2,
  XCircle,
  ShoppingCart,
  Eye,
  FileText
} from "lucide-react";

const NoteCard = ({ note, apiAuthPost, onDelete, onEdit, onBuyNow, onViewNote, isPurchased }) => {
  const [buying, setBuying] = useState(false);

  // Handle Delete request
  const handleDelete = async () => {
    if (!window.confirm("Are you sure you want to delete this note?")) return;

    try {
      if (apiAuthPost) {
        await apiAuthPost("/api/v1/notes/delete", { id: note.id });
      }
      if (onDelete) onDelete(note.id);
    } catch (err) {
      console.error("Failed to delete note:", err);
    }
  };

  // Helper to open Razorpay Modal
  const initializeRazorpayCheckout = (orderData) => {
    const options = {
      key: orderData.razorpayKeyId,
      amount: orderData.amountInSubunits,
      currency: orderData.currency || "INR",
      name: "Notes Platform",
      description: `Purchase: ${note.title || "Note Access"}`,
      order_id: orderData.gatewayOrderId,
      handler: async function (response) {
        try {
          // Verify payment signature on backend
          if (apiAuthPost) {
            await apiAuthPost("/api/payments/verify", {
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
              noteId: note.id,
            });
          }
          alert("Payment successful! Access granted.");
          window.location.reload(); // Refresh to reflect purchased state
        } catch (error) {
          console.error("Payment verification failed:", error);
          alert("Payment completed, but verification failed. Please refresh.");
        } finally {
          setBuying(false);
        }
      },
      modal: {
        ondismiss: function () {
          setBuying(false); // Re-enable button if user closes Razorpay modal
        },
      },
      prefill: {
        email: note.userEmail || "",
      },
      theme: {
        color: "#4f46e5",
      },
    };

    const rzp = new window.Razorpay(options);
    rzp.open();
  };

  // Handle Buy Now click
  const handleBuyNow = async () => {
    if (buying) return;
    setBuying(true);

    try {
      if (onBuyNow) {
        await onBuyNow(note);
        setBuying(false);
      } else if (apiAuthPost) {
        // Send request to Spring Boot endpoint (POST /api/payments/create-order)
        const orderData = await apiAuthPost("/api/payments/create-order", {
          noteId: note.id,
        });

        if (orderData && orderData.gatewayOrderId) {
          initializeRazorpayCheckout(orderData);
        } else {
          throw new Error("Invalid order response from server");
        }
      }
    } catch (err) {
      console.error("Failed to initiate purchase:", err);
      alert("Unable to process purchase. Please try again.");
      setBuying(false);
    }
  };

  // Convert subunits (e.g. 100 paise) to formatted price (₹1.00)
  const formatPrice = (subunits, currency) => {
    if (subunits === undefined || subunits === null || subunits === 0) return "Free";
    const amount = subunits / 100;
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: currency || "INR",
      maximumFractionDigits: 2,
    }).format(amount);
  };

  // Format date helper
  const formatDate = (dateString) => {
    if (!dateString) return null;
    return new Date(dateString).toLocaleDateString("en-IN", {
      month: "short",
      day: "numeric",
      year: "numeric",
    });
  };

  // Check entitlement status passed from parent or note payload
  const hasAccess = isPurchased || note.isEntitled || note.isOwner;

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between group">
      <div>
        {/* Card Top: Title & Actions */}
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="text-lg font-semibold text-slate-800 line-clamp-1 group-hover:text-indigo-600 transition-colors">
            {note.title?.trim() || "Untitled Note"}
          </h3>
          <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity">
            {onEdit && (
              <button
                onClick={() => onEdit(note)}
                className="p-1.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                title="Edit note"
              >
                <Edit3 className="w-4 h-4" />
              </button>
            )}
            {onDelete && (
              <button
                onClick={handleDelete}
                className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="Delete note"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>

        {/* Status & Price Badges */}
        <div className="flex items-center gap-2 mb-3">
          {note.isPublished !== undefined && (
            <span
              className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-medium ${
                note.isPublished
                  ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                  : "bg-slate-100 text-slate-600 border border-slate-200"
              }`}
            >
              {note.isPublished ? (
                <>
                  <CheckCircle2 className="w-3 h-3 text-emerald-600" /> Published
                </>
              ) : (
                <>
                  <XCircle className="w-3 h-3 text-slate-400" /> Draft
                </>
              )}
            </span>
          )}

          <span className="inline-flex items-center gap-1 text-xs px-2.5 py-0.5 bg-indigo-50 text-indigo-700 font-bold rounded-full border border-indigo-100">
            <Tag className="w-3 h-3" />
            {formatPrice(note.priceInSubunits, note.currency)}
          </span>
        </div>

        {/* Description / Content */}
        <p className="text-slate-600 text-sm line-clamp-4 leading-relaxed mb-4 whitespace-pre-line">
          {note.description || "No description provided."}
        </p>
      </div>

      {/* Card Footer: Metadata & Dynamic Access/Buy Button */}
      <div className="pt-3 border-t border-slate-100 flex flex-col gap-3">
        <div className="flex items-center justify-between text-xs text-slate-400">
          <span className="font-mono">ID: #{note.id}</span>

          {note.createdAt && (
            <div className="flex items-center gap-1" title={`Updated: ${note.updatedAt}`}>
              <Calendar className="w-3.5 h-3.5" />
              <span>{formatDate(note.createdAt)}</span>
            </div>
          )}
        </div>

        {/* Dynamic Action Button */}
        {hasAccess ? (
          <button
            onClick={() => onViewNote && onViewNote(note)}
            className="w-full flex items-center justify-center gap-2 py-2 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition-all"
          >
            <Eye className="w-4 h-4" /> View Note
          </button>
        ) : (
          <button
            onClick={handleBuyNow}
            disabled={buying || !note.isPublished}
            className="w-full flex items-center justify-center gap-2 py-2 px-4 bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition-all disabled:bg-slate-300 disabled:cursor-not-allowed"
          >
            {buying ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <ShoppingCart className="w-4 h-4" />
            )}
            {buying ? "Processing..." : "Buy Now"}
          </button>
        )}
      </div>
    </div>
  );
};

export default NoteCard;