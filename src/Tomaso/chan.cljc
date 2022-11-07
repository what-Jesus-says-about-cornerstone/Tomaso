(ns Tomaso.chan
  #?(:cljs (:require-macros [Tomaso.chan]))
  (:require
   [clojure.core.async :as a :refer [chan go go-loop <! >!  take! put! offer! poll! alt! alts! close!
                                     pub sub unsub mult tap untap mix admix unmix pipe
                                     timeout to-chan  sliding-buffer dropping-buffer
                                     pipeline pipeline-async]]
   [clojure.spec.alpha :as s]
   [galactica.csp.op.spec :as op.spec]
   [Tomaso.spec :as Tomaso.spec]))

(do (clojure.spec.alpha/check-asserts true))

(defmulti ^{:private true} op* op.spec/op-spec-dispatch-fn)
(s/def ::op (s/multi-spec op* op.spec/op-spec-retag-fn))
(defmulti op op.spec/op-dispatch-fn)

(s/def ::pubsub| some?)
(s/def ::pubsub|m some?)

(defn create-channels
  []
  (let [ops| (chan 10)]
    {::ops| ops|}))

(defmethod op*
  {::op.spec/op-key ::init} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::init}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))

(defmethod op*
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels]
   (op op-meta  channels (chan 1)))
  ([op-meta channels out|]
   (put! (::ops| channels) (merge op-meta
                                  {::op.spec/out| out|}))
   out|))


(defmethod op*
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req [::Tomaso.spec/id]))

(defmethod op
  {::op.spec/op-key ::id
   ::op.spec/op-type ::op.spec/request-response
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| value))


(defmethod op*
  {::op.spec/op-key ::request-pubsub-stream
   ::op.spec/op-type ::op.spec/request-stream
   ::op.spec/op-orient ::op.spec/request} [_]
  (s/keys :req [::Tomaso.spec/topic-id]))

(defmethod op
  {::op.spec/op-key ::request-pubsub-stream
   ::op.spec/op-type ::op.spec/request-stream
   ::op.spec/op-orient ::op.spec/request}
  ([op-meta channels value]
   (op op-meta  channels value (chan 64)))
  ([op-meta channels value out|]
   (put! (::ops| channels) (merge op-meta
                                  value
                                  {::op.spec/out| out|}))
   out|))


(defmethod op*
  {::op.spec/op-key ::request-pubsub-stream
   ::op.spec/op-type ::op.spec/request-stream
   ::op.spec/op-orient ::op.spec/response} [_]
  (s/keys :req []))

(defmethod op
  {::op.spec/op-key ::request-pubsub-stream
   ::op.spec/op-type ::op.spec/request-stream
   ::op.spec/op-orient ::op.spec/response}
  [op-meta out| value]
  (put! out| value))


(defmethod op*
  {::op.spec/op-key ::pubsub-publish
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req [::Tomaso.spec/topic-id]))

(defmethod op
  {::op.spec/op-key ::pubsub-publish
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))


(defmethod op*
  {::op.spec/op-key ::pubsub-sub
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req [::Tomaso.spec/topic-id]))

(defmethod op
  {::op.spec/op-key ::pubsub-sub
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))

(defmethod op*
  {::op.spec/op-key ::pubsub-unsub
   ::op.spec/op-type ::op.spec/fire-and-forget} [_]
  (s/keys :req [::Tomaso.spec/topic-id]))

(defmethod op
  {::op.spec/op-key ::pubsub-unsub
   ::op.spec/op-type ::op.spec/fire-and-forget}
  [op-meta channels value]
  (put! (::ops| channels) (merge op-meta
                                 value)))