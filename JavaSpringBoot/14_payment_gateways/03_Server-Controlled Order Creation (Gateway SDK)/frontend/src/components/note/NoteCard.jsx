import React, { useState, useEffect } from "react";
import {
  Calendar,
  Tag,
  CheckCircle2,
  ShoppingCart,
  Download,
} from "lucide-react";

const NoteCard = ({ note, apiAuthPost, onPaymentSuccess }) => {
  const [buying, setBuying] = useState(false);

  // Load Razorpay Script dynamically if not already loaded
  useEffect(() => {
    if (!window.Razorpay) {
      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.async = true;
      document.body.appendChild(script);
    }
  }, []);

  // Handler to download/open the note asset for purchased notes
  const handleDownloadNote = () => {
    if (note.accessUrl) {
      // Create a temporary anchor to trigger browser download/open
      const link = document.createElement("a");
      link.href = note.accessUrl;
      link.target = "_blank";
      link.rel = "noopener noreferrer";
      // Optional: setting download attribute if cross-origin allows
      link.setAttribute("download", `${note.title || "Note"}.pdf`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } else {
      alert("Download link is currently unavailable. Please refresh or try again later.");
    }
  };

  // Helper to open Razorpay Modal
  const initializeRazorpayCheckout = (orderData) => {
    if (!window.Razorpay) {
      alert("Razorpay SDK failed to load. Are you online?");
      setBuying(false);
      return;
    }

    const options = {
      key: orderData.razorpayKeyId,
      amount: orderData.amountInSubunits,
      currency: orderData.currency || "INR",
      name: "Notes Platform",
      description: `Purchase: ${note.title || "Note Access"}`,
      order_id: orderData.gatewayOrderId,
      handler: async function (response) {
        try {
          if (apiAuthPost) {
            await apiAuthPost("/api/payments/verify", {
              razorpay_order_id: response.razorpay_order_id,
              razorpay_payment_id: response.razorpay_payment_id,
              razorpay_signature: response.razorpay_signature,
              noteId: note.id,
            });
          }
          alert("Payment successful! Note unlocked.");
          if (onPaymentSuccess) {
            onPaymentSuccess(); // Refresh notes so backend returns isPurchased: true & accessUrl
          } else {
            window.location.reload();
          }
        } catch (error) {
          console.error("Payment verification failed:", error);
          alert("Payment completed, but verification failed. Please refresh.");
        } finally {
          setBuying(false);
        }
      },
      modal: {
        ondismiss: function () {
          setBuying(false);
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
    rzp.on("payment.failed", function (response) {
      alert(`Payment failed: ${response.error.description}`);
      setBuying(false);
    });
    rzp.open();
  };

  // Handle Buy Now click
  const handleBuyNow = async () => {
    if (buying) return;
    setBuying(true);

    try {
      const response = await apiAuthPost("/api/payments/create-order", {
        noteId: note.id,
      });

      const orderData = response?.data || response;

      if (orderData && orderData.gatewayOrderId) {
        initializeRazorpayCheckout(orderData);
      } else {
        throw new Error("Invalid order data received from server.");
      }
    } catch (err) {
      console.error("Failed to initiate purchase:", err);
      alert("Unable to process purchase. Please try again.");
      setBuying(false);
    }
  };

  // Convert subunits (e.g. 100 paise) to formatted currency (₹1.00)
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

  // Determine if current user owns/purchased this note
  const isPurchased = Boolean(note.isPurchased || note.accessUrl);

  return (
    <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:shadow-md transition-all flex flex-col justify-between group h-full">
      <div>
        {/* Title */}
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="text-lg font-semibold text-slate-800 line-clamp-1 group-hover:text-indigo-600 transition-colors">
            {note.title?.trim() || "Untitled Note"}
          </h3>
        </div>

        {/* Status / Price Badges */}
        <div className="flex flex-wrap items-center gap-2 mb-3">
          {isPurchased ? (
            <span className="inline-flex items-center gap-1 text-xs px-2.5 py-0.5 bg-emerald-50 text-emerald-700 font-bold rounded-full border border-emerald-200">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> Purchased
            </span>
          ) : (
            <span className="inline-flex items-center gap-1 text-xs px-2.5 py-0.5 bg-indigo-50 text-indigo-700 font-bold rounded-full border border-indigo-100">
              <Tag className="w-3.5 h-3.5" />
              {formatPrice(note.priceInSubunits, note.currency)}
            </span>
          )}

          {note.isPublished !== undefined && (
            <span
              className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-medium ${
                note.isPublished
                  ? "bg-slate-100 text-slate-600 border border-slate-200"
                  : "bg-amber-50 text-amber-700 border border-amber-200"
              }`}
            >
              {note.isPublished ? "Published" : "Draft"}
            </span>
          )}
        </div>

        {/* Description */}
        <p className="text-slate-600 text-sm line-clamp-4 leading-relaxed mb-4 whitespace-pre-line">
          {note.description || "No description provided."}
        </p>
      </div>

      {/* Card Footer */}
      <div className="pt-3 border-t border-slate-100 flex flex-col gap-3 mt-auto">
        <div className="flex items-center justify-between text-xs text-slate-400">
          <span className="font-mono">ID: #{note.id}</span>

          {note.createdAt && (
            <div className="flex items-center gap-1" title={`Created: ${note.createdAt}`}>
              <Calendar className="w-3.5 h-3.5" />
              <span>{formatDate(note.createdAt)}</span>
            </div>
          )}
        </div>

        {/* Button Action Switch */}
        {isPurchased ? (
          <button
            onClick={handleDownloadNote}
            className="w-full flex items-center justify-center gap-2 py-2 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-medium text-sm rounded-lg shadow-sm hover:shadow transition-all"
          >
            <Download className="w-4 h-4" />
            Download Note
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