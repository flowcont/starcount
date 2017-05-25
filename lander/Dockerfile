from base/archlinux

RUN pacman -Syy && \
    yes | pacman -S xinetd pv
COPY usr /usr
COPY etc /etc
RUN mkdir -p /data
ENTRYPOINT ["xinetd"]
CMD ["-d"]
