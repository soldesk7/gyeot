import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

export default function CategoryPicker() {
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState(false);

    useEffect(() => {
        fetch("/mock/guides/list.json")
            .then((response) => {
                if (!response.ok) {
                    throw new Error("카테고리 조회 실패");
                }
            return response.json();
        })
            .then((data) => setCategories(data))
            .catch((error) => setError(true));
    }, []);

    return (
        <main className="page">
            <h1>증상 직접 선택</h1>

            {error && (
                <p>증상 목록을 불러오지 못했어요. 잠시 후 다시 시도해주세요.</p>
            )}

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