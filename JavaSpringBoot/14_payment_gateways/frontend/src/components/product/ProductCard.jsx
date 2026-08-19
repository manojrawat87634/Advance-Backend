import React, { useState } from "react";
import { CheckCircle, Tag, CreditCard, Loader2 } from "lucide-react";

const ProductCard = ({ item, apiAuthPost }) => {
  const [isProcessing, setIsProcessing] = useState(false);

  // Helper to dynamically load the Razorpay SDK script
  const loadRazorpayScript = () => {
    return new Promise((resolve) => {
      if (window.Razorpay) {
        resolve(true);
        return;
      }
      const script = document.createElement("script");
      script.src = "https://checkout.razorpay.com/v1/checkout.js";
      script.onload = () => resolve(true);
      script.onerror = () => resolve(false);
      document.body.appendChild(script);
    });
  };

  // Handle Order Creation and Razorpay Checkout
  const handleBuyNow = async () => {
    try {

      // 1. Ensure Razorpay SDK is available
      const isScriptLoaded = await loadRazorpayScript();
      if (!isScriptLoaded) {
        alert("Razorpay SDK failed to load. Please check your internet connection.");
        return;
      }

      // 2. Call Spring Boot backend to generate a Razorpay Order
      const orderData = await apiAuthPost("/api/orders/create", { itemId: item.id }, setIsProcessing);

      if (!orderData || !orderData.razorpayOrderId) {
        alert("Could not initialize order. Please try again.");
        return;
      }
      console.log(orderData);
      // 3. Configure Razorpay modal options
      const options = {
       key: orderData.keyId, // ✅ CORRECT: matches keyId returned in CreateOrderResponse DTO
  amount: orderData.amount,
  currency: orderData.currency || "INR",
  name: "E-Commerce Store",
  description: `Purchase ${item.name}`,
  order_id: orderData.razorpayOrderId,
        handler: async function (response) {
          try {
            const verificationPayload = {
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            };

            const verificationResult = await apiAuthPost("/api/payments/verify", verificationPayload, ()=>{});

            if (verificationResult?.status === "SUCCESS" || verificationResult === true) {
              alert("Payment Successful! Order placed successfully.");
            } else {
              alert("Payment verification failed. Please contact support.");
            }
          } catch (err) {
            console.error("Verification error:", err);
            alert("Error verifying payment signature.");
          }
        },
        prefill: {
          name: "John Doe",
          email: "john@example.com",
          contact: "9999999999",
        },
        theme: {
          color: "#4f46e5",
        },
      };

      const razorpayWindow = new window.Razorpay(options);
      razorpayWindow.open();
    } catch (err) {
      console.error("Buy now error:", err);
      alert("Failed to initiate payment. Please try again.");
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="bg-white rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow duration-200 flex flex-col justify-between overflow-hidden group">
      {/* Top Details */}
      <div className="p-5">
        <div className="flex items-center justify-between mb-3">
          <span className="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
            <CheckCircle className="w-3.5 h-3.5" />
            In Stock
          </span>
          <span className="text-xs font-mono text-slate-400">#{item.id}</span>
        </div>

        <h3 className="text-lg font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors line-clamp-1">
          {item.name}
        </h3>

        <p className="text-sm text-slate-600 mt-2 line-clamp-2 leading-relaxed">
          {item.description || "No description provided."}
        </p>
      </div>

      {/* Pricing & Action Button */}
      <div className="bg-slate-50 px-5 py-4 border-t border-slate-100 flex flex-col gap-3">
        <div className="flex items-baseline justify-between">
          <div>
            <span className="text-xs text-slate-400 block font-medium">PRICE</span>
            <span className="text-2xl font-bold text-slate-900">
              ₹{item.priceInRupees?.toLocaleString("en-IN", { minimumFractionDigits: 2 })}
            </span>
          </div>

          <span className="text-xs font-mono text-slate-400 flex items-center gap-1">
            <Tag className="w-3 h-3" />
            {item.priceInPaise} paise
          </span>
        </div>

        <button
          onClick={handleBuyNow}
          disabled={isProcessing}
          className="w-full py-2.5 px-4 bg-indigo-600 hover:bg-indigo-700 active:bg-indigo-800 disabled:bg-indigo-400 text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 flex items-center justify-center gap-2 cursor-pointer disabled:cursor-not-allowed"
        >
          {isProcessing ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Processing...</span>
            </>
          ) : (
            <>
              <CreditCard className="w-4 h-4" />
              <span>Buy Now</span>
            </>
          )}
        </button>
      </div>
    </div>
  );
};

export default ProductCard;