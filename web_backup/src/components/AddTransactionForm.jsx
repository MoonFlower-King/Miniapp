import React, { useState } from 'react';
import { CATEGORIES } from '../utils/constants';
import { Check, X } from 'lucide-react';

export function AddTransactionForm({ onAdd, onClose }) {
    const [type, setType] = useState('expense');
    const [amount, setAmount] = useState('');
    const [category, setCategory] = useState('');
    const [note, setNote] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!amount || !category) return;

        onAdd({
            type,
            amount: parseFloat(amount),
            category,
            note,
            date: new Date().toISOString()
        });
        onClose();
    };

    return (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
            <div className="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-6 animate-in slide-in-from-bottom-10 fade-in duration-300">
                <div className="flex justify-between items-center mb-6">
                    <h2 className="text-xl font-bold text-gray-900">记一笔</h2>
                    <button onClick={onClose} className="p-2 bg-gray-100 rounded-full hover:bg-gray-200 transition-colors">
                        <X className="w-5 h-5 text-gray-600" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* Type Switcher */}
                    <div className="grid grid-cols-2 bg-gray-100 p-1 rounded-xl">
                        <button
                            type="button"
                            onClick={() => { setType('expense'); setCategory(''); }}
                            className={`py-2 rounded-lg text-sm font-semibold transition-all ${type === 'expense' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700'
                                }`}
                        >
                            支出
                        </button>
                        <button
                            type="button"
                            onClick={() => { setType('income'); setCategory(''); }}
                            className={`py-2 rounded-lg text-sm font-semibold transition-all ${type === 'income' ? 'bg-white shadow text-gray-900' : 'text-gray-500 hover:text-gray-700'
                                }`}
                        >
                            收入
                        </button>
                    </div>

                    {/* Amount Input */}
                    <div>
                        <label className="block text-xs font-medium text-gray-500 uppercase mb-1">金额</label>
                        <div className="relative">
                            <span className="absolute left-0 top-1/2 -translate-y-1/2 text-2xl font-bold text-gray-400">¥</span>
                            <input
                                type="number"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                placeholder="0.00"
                                className="w-full bg-transparent border-b-2 border-gray-100 py-2 pl-6 text-3xl font-bold text-gray-900 focus:outline-none focus:border-blue-600 placeholder-gray-200"
                                autoFocus
                            />
                        </div>
                    </div>

                    {/* Categories */}
                    <div>
                        <label className="block text-xs font-medium text-gray-500 uppercase mb-3">分类</label>
                        <div className="grid grid-cols-4 gap-3">
                            {CATEGORIES[type].map((cat) => {
                                const Icon = cat.icon;
                                const isSelected = category === cat.id;
                                return (
                                    <button
                                        key={cat.id}
                                        type="button"
                                        onClick={() => setCategory(cat.id)}
                                        className={`flex flex-col items-center gap-2 p-2 rounded-xl transition-all ${isSelected ? 'bg-blue-50 text-blue-600 scale-105' : 'text-gray-500 hover:bg-gray-50'
                                            }`}
                                    >
                                        <div className={`p-2.5 rounded-full ${isSelected ? 'bg-blue-100' : 'bg-gray-100'}`}>
                                            <Icon className="w-5 h-5" />
                                        </div>
                                        <span className="text-xs font-medium">{cat.name}</span>
                                    </button>
                                );
                            })}
                        </div>
                    </div>

                    {/* Note */}
                    <div>
                        <label className="block text-xs font-medium text-gray-500 uppercase mb-2">备注 (选填)</label>
                        <input
                            type="text"
                            value={note}
                            onChange={(e) => setNote(e.target.value)}
                            placeholder="写点什么..."
                            className="w-full bg-gray-50 rounded-xl px-4 py-3 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-100 transition-shadow"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={!amount || !category}
                        className="w-full bg-slate-900 text-white py-4 rounded-xl font-bold text-lg shadow-lg shadow-slate-200 active:scale-[0.98] transition-all disabled:opacity-50 disabled:shadow-none"
                    >
                        保存
                    </button>
                </form>
            </div>
        </div>
    );
}
