import { useState, useEffect, useMemo } from 'react';

const STORAGE_KEY = 'pocket_ledger_data';

export const useLedger = () => {
    const [transactions, setTransactions] = useState(() => {
        try {
            const saved = localStorage.getItem(STORAGE_KEY);
            return saved ? JSON.parse(saved) : [];
        } catch (e) {
            console.error('Failed to load transactions', e);
            return [];
        }
    });

    useEffect(() => {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(transactions));
        } catch (e) {
            console.error('Failed to save transactions', e);
        }
    }, [transactions]);

    const addTransaction = (transaction) => {
        const newTransaction = {
            id: crypto.randomUUID(),
            date: new Date().toISOString(), // Default to now if not provided, but form should provide it
            ...transaction
        };
        setTransactions(prev => [newTransaction, ...prev]);
    };

    const deleteTransaction = (id) => {
        setTransactions(prev => prev.filter(t => t.id !== id));
    };

    const stats = useMemo(() => {
        return transactions.reduce((acc, curr) => {
            const amount = parseFloat(curr.amount) || 0;
            if (curr.type === 'income') {
                acc.income += amount;
            } else {
                acc.expense += amount;
            }
            acc.balance = acc.income - acc.expense;
            return acc;
        }, { income: 0, expense: 0, balance: 0 });
    }, [transactions]);

    return {
        transactions,
        addTransaction,
        deleteTransaction,
        stats
    };
};
