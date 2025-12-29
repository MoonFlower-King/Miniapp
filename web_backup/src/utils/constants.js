import {
    Wallet, Gift, TrendingUp, MoreHorizontal,
    Utensils, Bus, ShoppingBag, Film, Home, Stethoscope
} from 'lucide-react';

export const CATEGORIES = {
    income: [
        { id: 'salary', name: '工资', icon: Wallet },
        { id: 'bonus', name: '奖金', icon: Gift },
        { id: 'investment', name: '理财', icon: TrendingUp },
        { id: 'other_in', name: '其他', icon: MoreHorizontal },
    ],
    expense: [
        { id: 'food', name: '餐饮', icon: Utensils },
        { id: 'transport', name: '交通', icon: Bus },
        { id: 'shopping', name: '购物', icon: ShoppingBag },
        { id: 'entertainment', name: '娱乐', icon: Film },
        { id: 'housing', name: '居住', icon: Home },
        { id: 'medical', name: '医疗', icon: Stethoscope },
        { id: 'other_out', name: '其他', icon: MoreHorizontal },
    ]
};
