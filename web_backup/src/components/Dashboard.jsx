import React from 'react';
import { Wallet, TrendingUp, TrendingDown } from 'lucide-react';

export function Dashboard({ stats }) {
    const { balance, income, expense } = stats;

    return (
        <div className="space-y-4 mb-6">
            {/* Total Balance Card */}
            <div className="bg-gradient-to-br from-blue-600 to-indigo-700 rounded-3xl p-6 text-white shadow-xl shadow-blue-200 transform transition-all hover:scale-[1.02]">
                <div className="flex items-center space-x-2 opacity-90 mb-2">
                    <div className="bg-white/20 p-1.5 rounded-full backdrop-blur-sm">
                        <Wallet className="w-5 h-5 text-white" />
                    </div>
                    <span className="text-sm font-medium tracking-wide">总资产</span>
                </div>
                <div className="text-4xl font-extrabold tracking-tight">
                    ¥ {balance.toFixed(2)}
                </div>
                <div className="mt-4 text-xs opacity-70">
                    我的小账本
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-2 gap-4">
                {/* Income */}
                <div className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 flex flex-col justify-between">
                    <div className="flex items-center space-x-2 mb-3">
                        <div className="bg-green-100 p-2 rounded-full">
                            <TrendingUp className="w-5 h-5 text-green-600" />
                        </div>
                        <span className="text-sm font-medium text-gray-500">本月收入</span>
                    </div>
                    <div className="text-2xl font-bold text-gray-900 truncate">
                        ¥ {income.toFixed(2)}
                    </div>
                </div>

                {/* Expense */}
                <div className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 flex flex-col justify-between">
                    <div className="flex items-center space-x-2 mb-3">
                        <div className="bg-red-100 p-2 rounded-full">
                            <TrendingDown className="w-5 h-5 text-red-600" />
                        </div>
                        <span className="text-sm font-medium text-gray-500">本月支出</span>
                    </div>
                    <div className="text-2xl font-bold text-gray-900 truncate">
                        ¥ {expense.toFixed(2)}
                    </div>
                </div>
            </div>
        </div>
    );
}
