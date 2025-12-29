import React, { useState } from 'react';
import { useLedger } from './hooks/useLedger';
import { Dashboard } from './components/Dashboard';
import { TransactionList } from './components/TransactionList';
import { AddTransactionForm } from './components/AddTransactionForm';
import { Plus } from 'lucide-react';

function App() {
  const { transactions, addTransaction, deleteTransaction, stats } = useLedger();
  const [isFormOpen, setIsFormOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50 flex justify-center font-sans text-gray-900">
      <div className="w-full max-w-md bg-white min-h-screen shadow-2xl relative flex flex-col">
        {/* Header */}
        <header className="px-6 pt-10 pb-2 bg-white sticky top-0 z-10 bg-opacity-90 backdrop-blur-xl border-b border-gray-50">
          <div className="flex justify-between items-end mb-4">
            <div>
              <h1 className="text-3xl font-black text-slate-900 tracking-tighter">小账本</h1>
              <p className="text-sm text-gray-400 font-medium mt-1">记录每一笔美好</p>
            </div>
            <div className="text-right">
              <div className="text-xs font-bold text-gray-400 uppercase tracking-widest">Today</div>
              <div className="text-lg font-bold text-slate-700">
                {new Date().getDate()} <span className="text-sm font-medium">{new Date().toLocaleDateString('en-US', { month: 'short' })}</span>
              </div>
            </div>
          </div>
        </header>

        <main className="flex-1 px-6 pt-6 pb-32 overflow-y-auto custom-scrollbar">
          <Dashboard stats={stats} />
          <TransactionList transactions={transactions} onDelete={deleteTransaction} />
        </main>

        {/* FAB */}
        <div className="fixed bottom-10 left-1/2 -translate-x-1/2 z-30">
          <button
            onClick={() => setIsFormOpen(true)}
            className="bg-slate-900 text-white pl-5 pr-6 py-4 rounded-full shadow-2xl shadow-slate-300 hover:scale-105 active:scale-95 transition-all flex items-center justify-center space-x-2 group"
          >
            <div className="bg-slate-800 p-1 rounded-full group-hover:rotate-90 transition-transform duration-300">
              <Plus className="w-5 h-5" />
            </div>
            <span className="font-bold text-lg tracking-wide">记一笔</span>
          </button>
        </div>

        {isFormOpen && (
          <AddTransactionForm
            onAdd={addTransaction}
            onClose={() => setIsFormOpen(false)}
          />
        )}
      </div>
    </div>
  );
}

export default App;
