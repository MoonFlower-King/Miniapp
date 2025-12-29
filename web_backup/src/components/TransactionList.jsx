import React from 'react';
import { CATEGORIES } from '../utils/constants';
import { Trash2 } from 'lucide-react';

export function TransactionList({ transactions, onDelete }) {
    if (transactions.length === 0) {
        return (
            <div className="text-center py-10 text-gray-400">
                <p>还没有记录哦，快去记一笔吧！</p>
            </div>
        );
    }

    // Helper to find category info
    const getCategoryInfo = (type, catId) => {
        const list = CATEGORIES[type] || [];
        return list.find(c => c.id === catId) || list.find(c => c.id.startsWith('other')) || {};
    };

    return (
        <div className="space-y-3 pb-20">
            <h3 className="text-lg font-bold text-gray-800 px-1">最近记录</h3>
            {transactions.map((t) => {
                const { id, type, amount, category, date, note } = t;
                const catInfo = getCategoryInfo(type, category);
                const Icon = catInfo.icon;
                const isExpense = type === 'expense';

                return (
                    <div key={id} className="bg-white p-4 rounded-xl shadow-sm border border-gray-50 flex items-center justify-between group">
                        <div className="flex items-center space-x-3">
                            <div className={`p-2.5 rounded-full ${isExpense ? 'bg-red-50 text-red-500' : 'bg-green-50 text-green-500'}`}>
                                {Icon && <Icon className="w-5 h-5" />}
                            </div>
                            <div>
                                <div className="font-semibold text-gray-900">{catInfo.name || '未知'}</div>
                                <div className="text-xs text-gray-400">
                                    {new Date(date).toLocaleDateString()} {note && `· ${note}`}
                                </div>
                            </div>
                        </div>
                        <div className="flex items-center space-x-3">
                            <span className={`font-bold text-lg ${isExpense ? 'text-gray-900' : 'text-green-600'}`}>
                                {isExpense ? '-' : '+'}{parseFloat(amount).toFixed(2)}
                            </span>
                            <button
                                onClick={() => onDelete(id)}
                                className="text-gray-300 hover:text-red-500 transition-colors p-1"
                            >
                                <Trash2 className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                );
            })}
        </div>
    );
}
