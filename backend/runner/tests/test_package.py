"""Placeholder ensuring runner tests are discoverable."""


def test_runner_package_is_importable() -> None:
    import runner

    assert runner.__doc__ is not None
