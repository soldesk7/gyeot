import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

export default function CategoryPicker() {
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        fetch("/mock/guides/list.json")
            .then((response) => response.json())
            .then((data) => setCategories(data))
    }, [])

    return (
        <main className="page">
            <h1>증상 직접 선택</h1>
            {categories.map((category) => (
                <Link
                    key={category.category}
                    className="action"
                    to={`/guide/${category.category}`}
                >
                    {category.title}
                </Link>
            ))}
        </main>
    );
}