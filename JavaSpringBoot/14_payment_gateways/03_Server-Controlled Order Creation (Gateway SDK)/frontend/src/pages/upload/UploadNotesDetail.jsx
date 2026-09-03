import React, { useContext, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { DataContext } from '../../context';
import { FileText, IndianRupee, Tag, AlignLeft, CheckCircle2 } from 'lucide-react';

export const UpdateNoteDetails = () => {
  const { id } = useParams(); // Retrieves ID from route like /notes/update/:id
  const navigate = useNavigate();
  const { apiAuthPost } = useContext(DataContext); // Uses your context method for POST requests

  const [formData, setFormData] = useState({
    title: '',
    description: '',
    priceInSubunits: 0,
    currency: 'INR',
    isPublished: true,
  });

  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);

    const payload = {
      ...formData,
      priceInSubunits: Number(formData.priceInSubunits) * 100, // Converts rupees to subunits (paise)
    };

    // Matches your backend mapping: @PostMapping("/update/{id}")
    apiAuthPost(`/api/v1/notes/update/${id}`, payload, (response) => {
      setLoading(false);
      if (response) {
        console.log(response);
        // navigate('/products'); // Redirects after successful update
      }
    });
  };

  return (
    <div className="max-w-md mx-auto bg-white dark:bg-slate-900 rounded-2xl shadow-xl overflow-hidden border border-slate-100 dark:border-slate-800 transition-all mt-8">
      {/* Header Banner */}
      <div className="h-20 bg-gradient-to-r from-indigo-500 via-purple-500 to-pink-500 flex items-center px-6">
        <h1 className="text-white text-lg font-bold flex items-center gap-2">
          <FileText size={20} /> Complete Note Details (Step 2)
        </h1>
      </div>

      <form onSubmit={handleSubmit} className="p-6 space-y-4">
        {/* Title Input */}
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
            Note Title
          </label>
          <div className="relative flex items-center">
            <Tag size={16} className="absolute left-3 text-slate-400" />
            <input
              type="text"
              name="title"
              required
              value={formData.title}
              onChange={handleChange}
              placeholder="e.g., Complete Java Backend Notes"
              className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        {/* Description Input */}
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
            Description
          </label>
          <div className="relative">
            <AlignLeft size={16} className="absolute left-3 top-3 text-slate-400" />
            <textarea
              name="description"
              rows={3}
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe what these notes cover..."
              className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        {/* Price Input */}
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400 mb-1">
            Price (in ₹)
          </label>
          <div className="relative flex items-center">
            <IndianRupee size={16} className="absolute left-3 text-slate-400" />
            <input
              type="number"
              name="priceInSubunits"
              min="0"
              value={formData.priceInSubunits}
              onChange={handleChange}
              placeholder="0 for free"
              className="w-full pl-9 pr-3 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg text-sm text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>
        </div>

        {/* Publish Checkbox */}
        <div className="flex items-center justify-between pt-2 border-t border-slate-100 dark:border-slate-800">
          <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
            Publish Immediately
          </span>
          <input
            type="checkbox"
            name="isPublished"
            checked={formData.isPublished}
            onChange={handleChange}
            className="w-4 h-4 text-indigo-600 rounded focus:ring-indigo-500 border-slate-300 dark:border-slate-700"
          />
        </div>

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-slate-900 hover:bg-slate-800 dark:bg-slate-100 dark:hover:bg-white text-white dark:text-slate-900 text-sm font-semibold rounded-lg transition-colors shadow-md mt-6"
        >
          <CheckCircle2 size={16} />
          {loading ? 'Saving...' : 'Save & Update Note'}
        </button>
      </form>
    </div>
  );
};

export default UpdateNoteDetails;