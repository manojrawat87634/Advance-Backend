import React, { useContext, useEffect, useState } from "react";
import { DataContext } from "../../context";
import { Search, StickyNote, ArrowUpDown, RefreshCw, AlertCircle } from "lucide-react";
import NoteCard from "../../components/note/NoteCard";


const DisplayNotes = () => {
  const { apiGet, apiAuthPost } = useContext(DataContext);
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Pagination Metadata from Spring Page Response
  const [pageInfo, setPageInfo] = useState({
    totalElements: 0,
    totalPages: 1,
    currentPage: 0,
    isFirst: true,
    isLast: true,
  });

  // Filter & Sort State
  const [searchTerm, setSearchTerm] = useState("");
  const [sortBy, setSortBy] = useState("default");

  const fetchNotes = async () => {
    try {
      setLoading(true);
      setError(null);

      // Fetch data from Spring Boot API
      const response = await apiGet("/api/v1/notes", {}, setNotes);

      // Handle Spring Data Page structure (which can be response.data or response directly depending on apiGet implementation)
      const pageData = response?.content ? response : response?.data;

      if (pageData && Array.isArray(pageData.content)) {
        // Extract array from pageData.content
        setNotes(pageData.content);

        // Save pagination metadata
        setPageInfo({
          totalElements: pageData.totalElements ?? pageData.content.length,
          totalPages: pageData.totalPages ?? 1,
          currentPage: pageData.number ?? 0,
          isFirst: pageData.first ?? true,
          isLast: pageData.last ?? true,
        });
      } else if (Array.isArray(response)) {
        // Fallback if backend returns a plain List instead of Page
        setNotes(response);
        setPageInfo({
          totalElements: response.length,
          totalPages: 1,
          currentPage: 0,
          isFirst: true,
          isLast: true,
        });
      } else {
        setNotes([]);
      }
    } catch (err) {
      console.error("Failed to fetch notes:", err);
      setError("Failed to load notes. Please check your connection and try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotes();
  }, []);

  // Frontend Filtering & Sorting
  const filteredNotes = notes
    .filter(
      (note) =>
        note.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        note.description?.toLowerCase().includes(searchTerm.toLowerCase())
    )
    .sort((a, b) => {
      if (sortBy === "newest") return new Date(b.createdAt) - new Date(a.createdAt);
      if (sortBy === "oldest") return new Date(a.createdAt) - new Date(b.createdAt);
      if (sortBy === "price-low") return (a.priceInSubunits || 0) - (b.priceInSubunits || 0);
      if (sortBy === "price-high") return (b.priceInSubunits || 0) - (a.priceInSubunits || 0);
      return 0;
    });

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="flex items-center gap-3 bg-white px-6 py-4 rounded-xl shadow-sm border border-slate-200">
          <div className="w-6 h-6 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          <h1 className="text-sm font-medium text-slate-700">Loading Notes...</h1>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
        <div className="text-center p-6 bg-white rounded-xl border border-red-200 shadow-sm max-w-md w-full">
          <AlertCircle className="w-10 h-10 text-red-500 mx-auto mb-3" />
          <p className="text-slate-800 font-semibold mb-1">Error Loading Content</p>
          <p className="text-slate-500 text-sm mb-4">{error}</p>
          <button
            onClick={fetchNotes}
            className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors"
          >
            <RefreshCw className="w-4 h-4" /> Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8 font-sans">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between border-b border-slate-200 pb-6 mb-8 gap-4">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
              <StickyNote className="w-8 h-8 text-indigo-600" />
              Notes Dashboard
            </h1>
            <p className="text-slate-500 mt-1 text-sm">
              Showing {filteredNotes.length} of {pageInfo.totalElements} total notes.
            </p>
          </div>

          {/* Search & Sort */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1 sm:w-64">
              <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                type="text"
                placeholder="Search notes..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-white border border-slate-300 rounded-lg text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all shadow-sm"
              />
            </div>

            <div className="relative sm:w-52">
              <ArrowUpDown className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="w-full pl-9 pr-8 py-2 bg-white border border-slate-300 rounded-lg text-sm text-slate-800 appearance-none focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all shadow-sm cursor-pointer"
              >
                <option value="default">Sort by: Default</option>
                <option value="newest">Date: Newest First</option>
                <option value="oldest">Date: Oldest First</option>
                <option value="price-low">Price: Low to High</option>
                <option value="price-high">Price: High to Low</option>
              </select>
            </div>
          </div>
        </div>

        {/* Note Grid */}
        {filteredNotes.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-xl border border-dashed border-slate-300">
            <StickyNote className="w-12 h-12 text-slate-300 mx-auto mb-3" />
            <p className="text-slate-600 font-medium text-lg">No notes found</p>
            <p className="text-slate-400 text-sm mt-1">
              Try adjusting your search query or sorting options.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filteredNotes.map((note) => (
              <NoteCard
                key={note.id}
                note={note}
                apiAuthPost={apiAuthPost}
                onPaymentSuccess={fetchNotes}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default DisplayNotes;